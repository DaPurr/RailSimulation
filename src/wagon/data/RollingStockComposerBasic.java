package wagon.data;

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
	
	@Override
	public TrainService realizedComposition(TrainService service, ScheduledTrip trip) {
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
			SortedSet<ScheduledTrip> plannedTrips = timetable.getRoute(service);
			int currentDayOfWeek = Integer.MAX_VALUE;
			Composition currentComp = new Composition();
			boolean processTrip = true;
			for (ScheduledTrip trip : plannedTrips) {
				Composition tripComp = trip.getTrainService().getComposition();
				if (tripComp.equals(currentComp))
					processTrip = true;
				if (trip.getDayOfWeek() != currentDayOfWeek) {
					processTrip = true;
					currentDayOfWeek = trip.getDayOfWeek();
				}
				if (processTrip) {
					currentComp = tripComp;
					TripStub tripStub = new TripStub(trip.getTrainService().id(), trip.fromStation(), trip.toStation());
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
						entry.getArrivalStation());
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
		private int trainNr;
		
		public TripStub(int trainNr, Station from, Station to) {
			this.trainNr = trainNr;
			this.from = from;
			this.to= to;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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