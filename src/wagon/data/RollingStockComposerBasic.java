package wagon.data;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import wagon.rollingstock.*;
import wagon.timetable.*;

public class RollingStockComposerBasic implements RollingStockComposer {
	
	private Timetable timetable;
	private RealisationData rdata;
	
	private Map<Set<RollingStockUnit>, Map<Set<RollingStockUnit>, Double>> probabilities;
	
	private RandomGenerator random;
	
	public RollingStockComposerBasic(
			Timetable timetable,
			RealisationData rdata) {
		this.timetable = timetable;
		this.rdata = rdata;
		int seed = 0; // remove when done with debugging
		random = new MersenneTwister(seed);
		probabilities = new HashMap<>();
		
		estimateProbabilities();
	}
	
	@Override
	public Composition realizedComposition(Composition comp, ScheduledTrip trip) {
		double r = random.nextDouble();
		Map<Set<RollingStockUnit>, Double> map = probabilities.get(comp.getUnits());
		if (map == null)
			throw new IllegalStateException("No probabilities estimated for composition: " + comp);
		double sum = 0.0;
		SortedSet<Entry<Set<RollingStockUnit>, Double>> sortedEntries = 
				new TreeSet<>(new EntryComparator());
		sortedEntries.addAll(map.entrySet());
		for (Entry<Set<RollingStockUnit>, Double> entry : sortedEntries) {
			sum += entry.getValue();
			if (r < sum) {
				Composition realizedComp = new Composition(comp.id(), entry.getKey());
				return realizedComp;
			}
		}
		throw new IllegalStateException("Could not realize a composition");
	}
	
	private void estimateProbabilities() {
		MismatchCounter counts = new MismatchCounter();
		for (Composition comp : timetable.compositions()) {
			SortedSet<ScheduledTrip> plannedTrips = timetable.getRoute(comp);
			int currentDayOfWeek = Integer.MAX_VALUE;
			Set<RollingStockUnit> currentUnits = new HashSet<>();
			boolean processTrip = true;
			for (ScheduledTrip trip : plannedTrips) {
				Set<RollingStockUnit> units = trip.composition().getUnits();
				if (!currentUnits.equals(units)) {
					processTrip = true;
					currentUnits = units;
				}
				if (trip.getDayOfWeek() != currentDayOfWeek) {
					processTrip = true;
					currentDayOfWeek = trip.getDayOfWeek();
				}
				
				if (processTrip) {
					// increment counters: go through all trips in realisation data with same 
					// train number and day
					SortedSet<RealisationDataEntry> entries = rdata.getEntriesByTrain(comp.id());
					if (entries != null) {
						for (RealisationDataEntry entry : entries) {
							if (tripEqualsEntry(trip, entry))
								counts.incrementCount(
										trip.composition().getUnits(), 
										entry.getRealizedComposition().getUnits());
						}
					}
					processTrip = false;
				}
			}
		}
		
		// now that we have the counts, convert them to probabilities
		for (Set<RollingStockUnit> comp : counts.keySet()) {
			int count = 0;
			Set<Set<RollingStockUnit>> y = counts.mapOfComposition(comp).keySet();
			for (Set<RollingStockUnit> y_i : y) {
				int toAdd = counts.mapOfComposition(comp).get(y_i);
				count += toAdd;
			}
			Map<Set<RollingStockUnit>, Double> map = probabilities.get(comp);
			if (map == null) {
				map = new HashMap<>();
			}
			for (Set<RollingStockUnit> y_i : y) {
				double prob = (double) counts.getCount(comp, y_i)/count;
				map.put(y_i, prob);
			}
			probabilities.put(comp, map);
		}
	}
	
	private boolean tripEqualsEntry(ScheduledTrip trip, RealisationDataEntry entry) {
		boolean b1 = trip.composition().id() == entry.getTrainNr();
		boolean b2 = trip.fromStation().equals(entry.getDepartureStation());
		boolean b3 = trip.toStation().equals(entry.getArrivalStation());
		boolean b4 = trip.getDayOfWeek() == entry.getPlannedDepartureTime().getDayOfWeek().getValue();
		return b1 && b2 && b3 && b4;
	}
	
	@Override
	public String toString() {
		String s = "";
		for (Entry<Set<RollingStockUnit>, Map<Set<RollingStockUnit>, Double>> kingEntry : probabilities.entrySet()) {
			String x = parseUnits(kingEntry.getKey());
			s += x + " | ";
			s += kingEntry.getValue().toString();
			s += System.lineSeparator();
		}
		return s;
	}
	
	private String parseUnits(Collection<RollingStockUnit> units) {
		String s = "";
		boolean first = true;
		for (RollingStockUnit unit : units) {
			if (first) {
				first = false;
			} else {
				s += "-";
			}
			s += unit.toString();
		}
		return s;
	}
	
	private static class MismatchCounter {
		private Map<Set<RollingStockUnit>, Map<Set<RollingStockUnit>, Integer>> kingMap;
		
		public MismatchCounter() {
			kingMap = new HashMap<>();
		}
		
		public int getCount(Set<RollingStockUnit> x, Set<RollingStockUnit> y) {
			Map<Set<RollingStockUnit>, Integer> map = kingMap.get(x);
			if (map == null)
				return 0;
			Integer count = map.get(y);
			if (count == null)
				return 0;
			return count;
		}
		
		public int incrementCount(Set<RollingStockUnit> x, Set<RollingStockUnit> y) {
			Map<Set<RollingStockUnit>, Integer> map = kingMap.get(x);
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
		
		public Map<Set<RollingStockUnit>, Integer> mapOfComposition(Set<RollingStockUnit> comp) {
			Map<Set<RollingStockUnit>, Integer> map = kingMap.get(comp);
			if (map == null)
				return null;
			return new HashMap<>(map);
		}
		
		public Set<Entry<Set<RollingStockUnit>, Map<Set<RollingStockUnit>, Integer>>> entrySet() {
			return kingMap.entrySet();
		}
		
		public Set<Set<RollingStockUnit>> keySet() {
			return kingMap.keySet();
		}
		
		@Override
		public String toString() {
			String s = "";
			for (Entry<Set<RollingStockUnit>, Map<Set<RollingStockUnit>, Integer>> kingEntry : kingMap.entrySet()) {
				String x = parseUnits(kingEntry.getKey());
				s += x + " | ";
				s += kingEntry.getValue().toString();
				s += System.lineSeparator();
			}
			return s;
		}
		
		private String parseUnits(Collection<RollingStockUnit> units) {
			String s = "";
			boolean first = true;
			for (RollingStockUnit unit : units) {
				if (first) {
					first = false;
				} else {
					s += "-";
				}
				s += unit.toString();
			}
			return s;
		}
	}
	
	private static class EntryComparator implements Comparator<Entry<Set<RollingStockUnit>, Double>> {

		@Override
		public int compare(Entry<Set<RollingStockUnit>, Double> o1, Entry<Set<RollingStockUnit>, Double> o2) {
			int res1 = -Double.compare(o1.getValue(), o2.getValue());
			if (res1 != 0)
				return res1;
			return o1.getKey().toString().compareTo(o2.getKey().toString());
		}
		
	}

}