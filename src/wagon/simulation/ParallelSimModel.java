package wagon.simulation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import com.google.common.collect.*;

import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.timetable.*;

public class ParallelSimModel {
	
	private final int horizon = 24*60*60; // simulation horizon in seconds

	private long seed;
	private Options options;
	private CiCoData cicoData;
	private Timetable timetable;
	private Map<Journey, ArrivalProcess> arrivalProcesses;
	private RollingStockComposer rcomposer;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public ParallelSimModel(
			Timetable timetable, 
			RealisationData rdata, 
			Options options) {
		seed = 0;
		this.options = options;
		this.timetable = timetable;
		
		cicoData = null;

		try {
			// import CiCoData
			if (options.getPathToCiCoData() == null)
				throw new NullPointerException("Path to CiCo data cannot be null");
			cicoData = CiCoData.importRawData(options);			
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (cicoData == null)
			throw new IllegalStateException("Something went wrong with passenger import");
		
		log.info("Fix CiCo data...");
		Set<Passenger> passengers = cicoData.getPassengers();
		Set<Passenger> passengersToDelete = new LinkedHashSet<>();
		Set<Station> availableStations = timetable.getStations();

		// remove passengers with origin or destination not in timetable
		for (Passenger passenger : passengers) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			if (!availableStations.contains(from) || !availableStations.contains(to)) {
				passengersToDelete.add(passenger);
			}
		}
		passengers.removeAll(passengersToDelete);
		
		log.info("...Fixed CiCo data");
		log.info("Passengers before removal: " + cicoData.getPassengers().size());
		log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
		log.info("Passengers remaining: " + passengers.size());
		cicoData.setPassengers(passengers);
		
		// estimate arrival processes
		log.info("Begin estimating arrival processes...");
		arrivalProcesses = estimateArrivalProcesses(cicoData);
		log.info("...Finish estimating arrival processes");
		
		// estimate mismatch probabilities
		log.info("Begin estimating mismatch probabilities...");
		rcomposer = new RollingStockComposerBasic(timetable, rdata);
		log.info("...Finish estimating mismatch probabilities");
		
		// generate mismatches
//		log.info("Begin altering timetable...");
//		generateMismatches(timetable, rcomposer);
//		log.info("...Finish altering timetable");
	}
	
	public ParallelReport start(int iterations) {
		if (iterations < 1)
			throw new IllegalArgumentException("Number of iterations must be > 0");
		// start parallel computing
		log.info("Start parallel computing...");
		int threads = Runtime.getRuntime().availableProcessors();
		threads /= 2;
//		int threads = 4;
		ExecutorService service = Executors.newFixedThreadPool(threads);
		Set<Future<Report>> futures = new HashSet<>();
		Set<Report> reports = Collections.newSetFromMap(new ConcurrentHashMap<>());
		for (int i = 0; i < iterations; i++) {
			Callable<Report> callable = new SimCallable();
			futures.add(service.submit(callable));
		}
		service.shutdown();
		try {
			// wait (almost) indefinitely
			service.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		log.info("...Finish parallel computing");

		// collect results
		try {
			for (Future<Report> future : futures) {
				reports.add(future.get());
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return new ParallelReport(reports, timetable.getAllTrips());
	}
	
	private Map<Journey, ArrivalProcess> estimateArrivalProcesses(CiCoData cicoData) {
		// group passengers based on their journeys
		Multimap<Journey, Passenger> map = LinkedHashMultimap.create();
		for (Passenger passenger : cicoData.getPassengers()) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			Journey journey = new Journey(from, to);
			map.put(journey, passenger);
		}

		// for each journey, estimate arrival process
		ConcurrentMap<Journey, ArrivalProcess> resultMap = new ConcurrentHashMap<>();
		double maxLambda = Double.NEGATIVE_INFINITY;
		for (Journey journey : map.keySet()) {
			Collection<Passenger> passengers = map.get(journey);
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, horizon, 5*60, seed);
//			ArrivalProcess arrivalProcess = new PiecewiseConstantProcess(passengers, 5*60, seed);
			resultMap.put(journey, arrivalProcess);
			double lambda = arrivalProcess.getLambdaUpperBound();
			if (lambda > maxLambda)
				maxLambda = lambda;
			seed++;
		}
		return resultMap;
	}
	
//	private void generateMismatches(Timetable timetable, RollingStockComposer composer) {
//		Set<Composition> compositions = timetable.compositions();
//		for (Composition comp : compositions) {
//			boolean generate = true;
//			Set<RollingStockUnit> currentPlannedUnits = new LinkedHashSet<>();
//			Composition realizedComposition = null;
//			SortedSet<ScheduledTrip> sortedTrips = timetable.getRoute(comp, options.getDayOfWeek());
//			if (sortedTrips != null) {
//				for (ScheduledTrip trip : sortedTrips) {
//					if (!currentPlannedUnits.equals(trip.composition().getUnits())) {
//						generate = true;
//					}
//					if (generate) {
//						realizedComposition = composer.realizedComposition(trip.composition(), trip);
//						generate = false;
//						currentPlannedUnits = trip.composition().getUnits();
//					}
//					trip.setComposition(realizedComposition);
//				}
//			}
//		}
//	}
	
	private class SimCallable implements Callable<Report> {

		@Override
		public Report call() throws Exception {
			Timetable realizedTimetable = new Timetable(timetable);
			SimModel sim = new SimModel(
					realizedTimetable, 
					arrivalProcesses, 
					cicoData, 
					rcomposer, 
					options);
			Report report = sim.start();
			return report;
		}
		
	}
}
