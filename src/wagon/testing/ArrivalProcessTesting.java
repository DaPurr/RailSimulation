package wagon.testing;

import java.time.LocalTime;
import java.util.*;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.network.*;
import wagon.network.expanded.*;
import wagon.simulation.*;
import wagon.timetable.*;

public class ArrivalProcessTesting {
	
	private Timetable timetable;
	private EventActivityNetwork network;
	private CiCoData cicoData;
	private RandomGenerator random;
	
	private final static LocalTime BASE_TIME = LocalTime.of(0, 0, 0);

	public ArrivalProcessTesting(Timetable timetable, CiCoData cicoData) {
		this.timetable = timetable;
		this.cicoData = cicoData;
		random = new MersenneTwister();
		network = EventActivityNetwork.createTransferNetwork(timetable, 2, 1);
	}
	
	public double[] calculateLoss(int[] widths) {
		int horizon = 24*60*60;
		
		double[] scores = new double[widths.length];
		
		// group passengers based on their journeys
		Multimap<Journey, Passenger> map = LinkedHashMultimap.create();
		for (Passenger passenger : cicoData.getPassengers()) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			Journey journey = new Journey(from, to);
			map.put(journey, passenger);
		}
		
		// get realized counts
		Map<ScheduledTrip, Integer> realizedCounts = new HashMap<>();
		
		Map<Journey, ArrivalProcess> realizedProcesses = new LinkedHashMap<>();
		int count = 0;
		for (Journey journey : map.keySet()) {
			count++;
			Collection<Passenger> passengers = map.get(journey);
			RealizedDataProcess realizedProcess = new RealizedDataProcess(passengers);
			realizedProcesses.put(journey, realizedProcess);
			
			if (count % 100 == 0)
				System.out.println("Estimated " + count+"/"+map.keySet().size() + " journeys...");
		}
		
		// generate arrivals
		count = 0;
		for (Journey journey : map.keySet()) {
			count++;
			ArrivalProcess realizedProcess = realizedProcesses.get(journey);
			List<Double> realizedArrivals = realizedProcess.generateArrivalsFromProcess(horizon);

			// determine routes
			// realized
			processArrivals(journey, realizedArrivals, realizedCounts);

			if (count % 100 == 0)
				System.out.println("Processed " + count+"/"+map.keySet().size() + " journeys...");
		}

		for (int i = 0; i < widths.length; i++) {
			int width = widths[i];
			
			Map<ScheduledTrip, Integer> estimatedCounts = new HashMap<>();
			
			// for each journey, estimate arrival process
			Map<Journey, ArrivalProcess> estimatedProcesses = new LinkedHashMap<>();
			count = 0;
			for (Journey journey : map.keySet()) {
				count++;
				Collection<Passenger> passengers = map.get(journey);
//				HybridArrivalProcess estimatedProcess = new HybridArrivalProcess(passengers, 0, horizon, width*60, random.nextLong());
				PiecewiseConstantProcess estimatedProcess = new PiecewiseConstantProcess(passengers, width*60, random.nextLong());
				estimatedProcesses.put(journey, estimatedProcess);
				
				if (count % 100 == 0)
					System.out.println("Estimated " + count+"/"+map.keySet().size() + " journeys...");
			}
			System.out.println("...Finished estimating " + count + " journeys");
			
			// generate arrivals
			count = 0;
			for (Journey journey : map.keySet()) {
				count++;
				ArrivalProcess estimatedProcess = estimatedProcesses.get(journey);
				List<Double> estimatedArrivals = estimatedProcess.generateArrivalsFromProcess(horizon);
				
				// determine routes
				// estimated
				processArrivals(journey, estimatedArrivals, estimatedCounts);
				
				if (count % 100 == 0)
					System.out.println("Processed " + count+"/"+map.keySet().size() + " journeys...");
			}
			
			double score = lossFunction(realizedCounts, estimatedCounts);
			scores[i] = score;
		}
		
		return scores;
	}
	
	private double lossFunction(Map<ScheduledTrip, Integer> map1, Map<ScheduledTrip, Integer> map2) {
		double score = 0.0;
		
		for (ScheduledTrip trip : timetable.getAllTrips(2)) {
			int count1 = getCountsFromTrip(trip, map1);
			int count2 = getCountsFromTrip(trip, map2);
			int diff = count1-count2;
			score += diff*diff;
		}
		return score;
	}
	
	private int getCountsFromTrip(ScheduledTrip trip, Map<ScheduledTrip, Integer> map) {
		Integer count = map.get(trip);
		if (count == null)
			return 0;
		return count;
	}
	
	private void processArrivals(Journey journey, List<Double> arrivals, Map<ScheduledTrip, Integer> counts) {
		for (double val : arrivals) {
			int arrivalTime = (int) Math.floor(val);
			RouteGeneration routeGen = new LexicographicallyFirstGeneration(
					network, 
					journey.origin.name(), 
					journey.destination.name(), 
					BASE_TIME.plusSeconds(arrivalTime));
			List<Path> paths = routeGen.generateRoutes();
			
			if (!hasNullRoute(paths)) {
				RouteSelection routeSelector = new EarliestArrivalSelector();
				Path plannedRoute = routeSelector.selectPath(paths);
				
				boolean add = true;
				for (WeightedEdge edge : plannedRoute.getEdges()) {
					if (edge instanceof TripEdge) {
						TripEdge tEdge = (TripEdge) edge;
						if (add) {
							add = false;
							increment(counts, tEdge.trip());
						}
					} else if (edge instanceof TransferEdge)
						add = true;
				}
			}
		}
	}
	
	private boolean hasNullRoute(List<Path> paths) {
		for (int i = 0; i < paths.size(); i++) {
			Path path = paths.get(i);
			if (path == null)
				return true;
		}
		return false;
	}
	
	private void increment(Map<ScheduledTrip, Integer> map, ScheduledTrip trip) {
		Integer count = map.get(trip);
		if (count == null) {
			count = 0;
		}
		map.put(trip, count+1);
	}
}
