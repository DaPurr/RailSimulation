package wagon.data;

import java.util.*;

import wagon.rollingstock.*;
import wagon.timetable.*;

public class RollingStockComposerBasic implements RollingStockComposer {
	
	private Timetable timetable;
	private RealisationData rdata;
	
	private Map<List<RollingStockUnit>, Map<RollingStockUnit, Double>> probabilities;
	
	public RollingStockComposerBasic(
			Timetable timetable,
			RealisationData rdata) {
		this.timetable = timetable;
		this.rdata = rdata;
		
		probabilities = new HashMap<>();
		
		estimateProbabilities();
	}
	
	@Override
	public Composition realizedComposition(Composition comp, int trainNumber, ScheduledTrip trip) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void estimateProbabilities() {
		MismatchCounter counts = new MismatchCounter();
		for (Composition comp : timetable.compositions()) {
			List<ScheduledTrip> plannedTrips = timetable.getRoute(comp);
			int currentDayOfWeek = Integer.MAX_VALUE;
			Set<RollingStockUnit> currentUnits = new HashSet<>();
			boolean processTrip = true;
			for (ScheduledTrip trip : plannedTrips) {
				Set<RollingStockUnit> units = new HashSet<>(trip.composition().getUnits());
				if (!currentUnits.equals(units))
					processTrip = true;
				if (trip.getDayOfWeek() != currentDayOfWeek)
					processTrip = true;
				
				if (processTrip) {
					// increment counters: go through all trips in realisation data with same 
					// train number and day
					SortedSet<RealisationDataEntry> entries = rdata.getEntriesByTrain(comp.id());
					for (RealisationDataEntry entry : entries) {
						if (tripEqualsEntry(trip, entry))
							counts.incrementCount(
									new HashSet<>(trip.composition().getUnits()), 
									new HashSet<>(entry.getRealizedComposition().getUnits()));
					}
				}
			}
		}
	}
	
	private boolean tripEqualsEntry(ScheduledTrip trip, RealisationDataEntry entry) {
		boolean b1 = trip.composition().id() == entry.getTrainNr();
		boolean b2 = trip.fromStation().equals(entry.getDepartureStation());
		boolean b3 = trip.toStation().equals(entry.getArrivalStation());
		return b1 && b2 && b3;
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
	}

}