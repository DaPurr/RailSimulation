package wagon.data;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import wagon.infrastructure.Station;
import wagon.rollingstock.*;
import wagon.timetable.*;

public class RollingStockComposerBasic implements RollingStockComposer {
	
	private Timetable timetable;
	private RealisationData rdata;
	
	private Map<Composition, Map<Composition, Double>> probabilities;
	private MismatchCounter misCounter;
	
	private RandomGenerator random;
	
	public RollingStockComposerBasic(
			Timetable timetable,
			RealisationData rdata, 
			long seed) {
		this.timetable = timetable;
		this.rdata = rdata;
		random = new MersenneTwister(seed);
		probabilities = new LinkedHashMap<>();
		
		estimateProbabilities();
	}
	
	private RollingStockComposerBasic(RandomGenerator random) {
		this.random = random;
	}
	
	/**
	 * Creates a shallow copy with random number stream according to <code>seed</code>.
	 * 
	 * @param rcomposer	the rolling stock composer
	 * @param seed		the seed
	 */
	public RollingStockComposerBasic(RollingStockComposerBasic rcomposer, long seed) {
		this.timetable = rcomposer.timetable;
		this.rdata = rcomposer.rdata;
		this.probabilities = rcomposer.probabilities;
		random = new MersenneTwister(seed);
	}
	
	public String summary() {
		String s = "";
		double countPlanned = 0;
		double countTotal = 0;
		double countLower = 0;
		double countHigher = 0;
		for (Composition x : misCounter.kingMap.keySet()) {
			Map<Composition, Integer> map = misCounter.kingMap.get(x);
			for (Composition y : map.keySet()) {
				countTotal += misCounter.getCount(x, y);
				if (x.equals(y)) {
					countPlanned += misCounter.getCount(x, y);
				} else {
					if (x.getAllSeats() < y.getAllSeats())
						countHigher += misCounter.getCount(x, y);
					if (x.getAllSeats() > y.getAllSeats())
						countLower += misCounter.getCount(x, y);
				}
			}
		}
		
		s += "Total: " + countTotal + System.lineSeparator();
		s += "Planned: " + countPlanned + "(" + (countPlanned/countTotal) + ")" + System.lineSeparator();
		s += "Lower: " + countLower + "(" + (countLower/(countTotal-countPlanned)) + ")" + System.lineSeparator();
		s += "Higher: " + countHigher + "(" + (countHigher/(countTotal-countPlanned)) + ")" + System.lineSeparator();
		
		return s;
	}
	
	@Override
	public TrainService realizedComposition(TrainService service, Trip trip) {
		double r = random.nextDouble();
		Map<Composition, Double> map = probabilities.get(service.getComposition());
		if (map == null) {
			throw new IllegalStateException("No probabilities estimated for composition: "
					+ service.getComposition());
		}
		double sum = 0.0;
		SortedSet<Entry<Composition, Double>> sortedEntries = 
				new TreeSet<>(new EntryComparator());
		sortedEntries.addAll(map.entrySet());
		for (Entry<Composition, Double> entry : sortedEntries) {
			sum += entry.getValue();
			if (r < sum) {
				TrainService realizedComp = new TrainService(service.id(), entry.getKey());
				return realizedComp;
			}
		}
		throw new IllegalStateException("Could not realize a composition");
	}
	
	private void estimateProbabilities() {
		MismatchCounter counts = new MismatchCounter();
		Map<TripStub, Composition> tripToPlannedComp = new HashMap<>();
		for (TrainService service : timetable.getTrainServices()) {
			SortedSet<Trip> plannedTrips = timetable.getRoute(service);
			int currentDayOfWeek = Integer.MAX_VALUE;
			Composition currentComp = new Composition();
			boolean processTrip = true;
			for (Trip trip : plannedTrips) {
				Composition tripComp = trip.getTrainService().getComposition();
				if (!tripComp.equals(currentComp))
					processTrip = true;
				if (trip.getDayOfWeek() != currentDayOfWeek) {
					processTrip = true;
					currentDayOfWeek = trip.getDayOfWeek();
				}
				if (processTrip) {
					currentComp = tripComp;
					TripStub tripStub = new TripStub(
							trip.getTrainService().id(), 
							trip.fromStation(), trip.toStation(), 
							trip.getDayOfWeek());
					tripToPlannedComp.put(tripStub, trip.getTrainService().getComposition());
					processTrip = false;
				}
			}
		}
		
		for (TrainService service : timetable.getTrainServices()) {
			SortedSet<RealisationDataEntry> entries = rdata.getEntriesByTrain(service.id());
			if (entries == null)
				continue;
			for (RealisationDataEntry entry : entries) {
				TripStub tripStub = new TripStub(
						entry.getTrainNr(), 
						entry.getDepartureStation(), 
						entry.getArrivalStation(), 
						entry.getPlannedDepartureTime().toLocalDate().getDayOfWeek().getValue());
				Composition plannedComp = tripToPlannedComp.get(tripStub);
				if (plannedComp == null)
					continue;
				counts.incrementCount(plannedComp, entry.getRealizedComposition().getComposition());
			}
		}
		
		// now that we have the counts, convert them to probabilities
		for (Composition comp : counts.keySet()) {
			int count = 0;
			Set<Composition> y = counts.mapOfComposition(comp).keySet();
			for (Composition y_i : y) {
				int toAdd = counts.mapOfComposition(comp).get(y_i);
				count += toAdd;
			}
			Map<Composition, Double> map = probabilities.get(comp);
			if (map == null) {
				map = new HashMap<>();
			}
			for (Composition y_i : y) {
				double prob = (double) counts.getCount(comp, y_i)/count;
				map.put(y_i, prob);
			}
			probabilities.put(comp, map);
		}
		
		misCounter = counts;
	}
	
	public RollingStockComposerBasic decreaseMismatches(double phi) {
		RollingStockComposerBasic rcomposer = new RollingStockComposerBasic(this.random);
		Map<Composition, Map<Composition, Double>> newProbs = deepCopyProbabilities();
		for (Entry<Composition, Map<Composition, Double>> outerEntry : newProbs.entrySet()) {
			Composition x = outerEntry.getKey();
			boolean hasX = false;
			
			// now recompute probabilities
			for (Entry<Composition, Double> innerEntry : outerEntry.getValue().entrySet()) {
				Composition y = innerEntry.getKey();
				double oldPi = innerEntry.getValue();
				if (x.equals(y)) {
					innerEntry.setValue(oldPi + phi*(1-oldPi));
					hasX = true;
				} else {
					innerEntry.setValue(oldPi*(1-phi));
				}
			}
			
			if (!hasX) {
				outerEntry.getValue().put(x, phi);
			}
		}
		
		rcomposer.probabilities = newProbs;
		return rcomposer;
	}
	
	private Map<Composition, Map<Composition, Double>> deepCopyProbabilities() {
		Map<Composition, Map<Composition, Double>> map = new HashMap<>();
		for (Entry<Composition, Map<Composition, Double>> outerEntry : probabilities.entrySet()) {
			Map<Composition, Double> y = new HashMap<>();
			for (Entry<Composition, Double> innerEntry : outerEntry.getValue().entrySet()) {
				y.put(innerEntry.getKey(), innerEntry.getValue().doubleValue());
			}
			map.put(outerEntry.getKey(), y);
		}
		return map;
	}
	
//	private boolean tripEqualsEntry(ScheduledTrip trip, RealisationDataEntry entry) {
//		boolean b1 = trip.getTrainService().id() == entry.getTrainNr();
//		boolean b2 = trip.fromStation().equals(entry.getDepartureStation());
//		boolean b3 = trip.toStation().equals(entry.getArrivalStation());
//		boolean b4 = trip.getDayOfWeek() == entry.getPlannedDepartureTime().getDayOfWeek().getValue();
//		return b1 && b2 && b3 && b4;
//	}
	
	@Override
	public String toString() {
		String s = "";
		for (Entry<Composition, Map<Composition, Double>> kingEntry : probabilities.entrySet()) {
			String x = kingEntry.getKey().toString();
			s += x + "\t|\t";
			Map<Composition, Double> map = kingEntry.getValue();
			boolean first = true;
			for (Entry<Composition, Double> entry : map.entrySet()) {
				if (first)
					first = false;
				else
					s += ",\t";;
				s += entry.toString();
			}
//			s += kingEntry.getValue().toString();
			s += System.lineSeparator();
		}
		return s;
	}
	
	public void exportProbabilities(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (Entry<Composition, Map<Composition, Double>> entry : probabilities.entrySet()) {
			Composition x = entry.getKey();
			Map<Composition, Double> xProbs = entry.getValue();
			bw.write(x.toString());
			for (Entry<Composition, Double> yEntry : xProbs.entrySet()) {
				bw.write("," + yEntry.getKey().toString() + ":" + yEntry.getValue());
			}
			bw.newLine();
		}
		bw.close();
	}
	
	public static RollingStockComposerBasic importFromFile(String fileName, long seed) throws IOException {
		Map<Composition, Map<Composition, Double>> probs = new LinkedHashMap<>();
		
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(",");
			Composition x = Composition.toComposition(parts[0]);
			Map<Composition, Double> yProbs = new LinkedHashMap<>();
			for (int i = 1; i < parts.length; i++) {
				String[] yParts = parts[i].split(":");
				Composition y = Composition.toComposition(yParts[0]);
				double prob = Integer.valueOf(yParts[1]);
				yProbs.put(y, prob);
			}
			probs.put(x, yProbs);
		}
		br.close();
		
		RollingStockComposerBasic rcomposer = new RollingStockComposerBasic(new MersenneTwister(seed));
		rcomposer.probabilities = probs;
		return rcomposer;
	}
	
	private static class MismatchCounter {
		private Map<Composition, Map<Composition, Integer>> kingMap;
		
		public MismatchCounter() {
			kingMap = new HashMap<>();
		}
		
		public int getCount(Composition x, Composition y) {
			Map<Composition, Integer> map = kingMap.get(x);
			if (map == null)
				return 0;
			Integer count = map.get(y);
			if (count == null)
				return 0;
			return count;
		}
		
		public int incrementCount(Composition x, Composition y) {
			Map<Composition, Integer> map = kingMap.get(x);
			if (map == null) {
				map = new HashMap<>();
				kingMap.put(x, map);
				map.put(y, 1);
				return 1;
			}
			Integer prevCount = map.get(y);
			if (prevCount == null) {
				map.put(y, 1);
				return 1;
			}
			map.put(y, prevCount + 1);
			return prevCount + 1;
		}
		
		public Map<Composition, Integer> mapOfComposition(Composition comp) {
			Map<Composition, Integer> map = kingMap.get(comp);
			if (map == null)
				return null;
			return new HashMap<>(map);
		}
		
		public Set<Composition> keySet() {
			return kingMap.keySet();
		}
		
		@Override
		public String toString() {
			String s = "";
			for (Entry<Composition, Map<Composition, Integer>> kingEntry : kingMap.entrySet()) {
				String x = kingEntry.getKey().toString();
				s += x + " | ";
				s += kingEntry.getValue().toString();
				s += System.lineSeparator();
			}
			return s;
		}
	}
	
	private static class EntryComparator implements Comparator<Entry<Composition, Double>> {

		@Override
		public int compare(Entry<Composition, Double> o1, Entry<Composition, Double> o2) {
			int res1 = -Double.compare(o1.getValue(), o2.getValue());
			if (res1 != 0)
				return res1;
			return o1.getKey().toString().compareTo(o2.getKey().toString());
		}
		
	}
	
	private static class Pair<U, V> {
		public final U x;
		public final V y;
		
		public Pair(U x, V y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Pair))
				return false;
			Pair<U,V> other = (Pair<U,V>) obj;
			if (x == null) {
				if (other.x != null)
					return false;
			} else if (!x.equals(other.x))
				return false;
			if (y == null) {
				if (other.y != null)
					return false;
			} else if (!y.equals(other.y))
				return false;
			return true;
		}
		
		
	}
	
	private static class TripStub {
		private Station from;
		private Station to;
		private int dayOfWeek;
		private int trainNr;
		
		public TripStub(int trainNr, Station from, Station to, int dayOfWeek) {
			this.trainNr = trainNr;
			this.from = from;
			this.to = to;
			this.dayOfWeek = dayOfWeek;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dayOfWeek;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			result = prime * result + trainNr;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof TripStub))
				return false;
			TripStub other = (TripStub) obj;
			if (dayOfWeek != other.dayOfWeek)
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			if (trainNr != other.trainNr)
				return false;
			return true;
		}
		
	}

}