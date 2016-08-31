package wagon.testing;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.*;

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
	private final static int HORIZON = 24*60*60;

	public ArrivalProcessTesting(Timetable timetable, CiCoData cicoData) {
		this.timetable = timetable;
		this.cicoData = cicoData;
		random = new MersenneTwister();
		network = EventActivityNetwork.createTransferNetwork(timetable, 2, 1);
	}
	
	public double[] calculateLoss(int[] widths) {
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
		Map<Trip, Integer> realizedCounts = new HashMap<>();
		
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
			List<Double> realizedArrivals = realizedProcess.generateArrivalsFromProcess(HORIZON);

			// determine routes
			// realized
			processArrivals(journey, realizedArrivals, realizedCounts);

			if (count % 100 == 0)
				System.out.println("Processed " + count+"/"+map.keySet().size() + " journeys...");
		}

		int parallelThreads = 4;
		int iterations = 16;
		for (int i = 0; i < widths.length; i++) {
			int width = widths[i];
			
			System.out.println("Start parallel computing...");
			ExecutorService service = Executors.newFixedThreadPool(parallelThreads);
			Set<Future<Map<Trip, Integer>>> futures = new HashSet<>();
			Set<Map<Trip, Integer>> counts = Collections.newSetFromMap(new ConcurrentHashMap<>());
			for (int j = 0; j < iterations; j++) {
				Callable<Map<Trip, Integer>> callable = new TestCallable(width, map, random);
				futures.add(service.submit(callable));
			}
			service.shutdown();
			try {
				// wait (almost) indefinitely
				service.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			System.out.println("...Finish parallel computing");
			
			// collect results
			try {
				for (Future<Map<Trip, Integer>> future : futures) {
					counts.add(future.get());
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			
			double score = lossFunction(realizedCounts, counts);
			scores[i] = score;
		}
		
		return scores;
	}
	
	private double lossFunction(Map<Trip, Integer> map1, Set<Map<Trip, Integer>> map2) {
		double score = 0.0;
		for (Trip trip : timetable.getAllTrips(2)) {
			int count1 = getCountsFromTrip(trip, map1);
			double expectedCount2 = 0.0;
			for (Map<Trip, Integer> map : map2) {
				expectedCount2 += getCountsFromTrip(trip, map);
			}
			expectedCount2 /= map2.size();
			
			double diff = count1-expectedCount2;
			score += diff*diff;
		}
		return score;
	}
	
	private int getCountsFromTrip(Trip trip, Map<Trip, Integer> map) {
		Integer count = map.get(trip);
		if (count == null)
			return 0;
		return count;
	}
	
	private void processArrivals(Journey journey, List<Double> arrivals, Map<Trip, Integer> counts) {
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
	
	private void increment(Map<Trip, Integer> map, Trip trip) {
		Integer count = map.get(trip);
		if (count == null) {
			count = 0;
		}
		map.put(trip, count+1);
	}
	
	private class TestCallable implements Callable<Map<Trip, Integer>> {
		
		private Multimap<Journey, Passenger> realizedMap;
		private int width;
		private RandomGenerator random;
		
		public TestCallable(int width, Multimap<Journey, Passenger> realizedMap, RandomGenerator random) {
			this.realizedMap = realizedMap;
			this.random = random;
			this.width = width;
		}

		@Override
		public Map<Trip, Integer> call() throws Exception {
			Map<Trip, Integer> estimatedCounts = new HashMap<>();
			
			// for each journey, estimate arrival process
			Map<Journey, ArrivalProcess> estimatedProcesses = new LinkedHashMap<>();
			long count = 0;
			for (Journey journey : realizedMap.keySet()) {
				count++;
				Collection<Passenger> passengers = realizedMap.get(journey);
				HybridArrivalProcess estimatedProcess = new HybridArrivalProcess(passengers, 0, HORIZON, width*60, random.nextLong());
//				PiecewiseConstantProcess estimatedProcess = new PiecewiseConstantProcess(passengers, width*60, random.nextLong());
				estimatedProcesses.put(journey, estimatedProcess);
				
				if (count % 100 == 0)
					System.out.println("Estimated " + count+"/"+realizedMap.keySet().size() + " journeys...");
			}
			System.out.println("...Finished estimating " + count + " journeys");
			
			// generate arrivals
			count = 0;
			for (Journey journey : realizedMap.keySet()) {
				count++;
				ArrivalProcess estimatedProcess = estimatedProcesses.get(journey);
				List<Double> estimatedArrivals = estimatedProcess.generateArrivalsFromProcess(HORIZON);
				
				// determine routes
				// estimated
				processArrivals(journey, estimatedArrivals, estimatedCounts);
				
				if (count % 100 == 0)
					System.out.println("Processed " + count+"/"+realizedMap.keySet().size() + " journeys...");
			}
			return estimatedCounts;
		}
		
	}
}
