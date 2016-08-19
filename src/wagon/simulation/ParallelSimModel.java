package wagon.simulation;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Logger;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.collect.*;

import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.timetable.*;

public class ParallelSimModel {
	
	private final int horizon = 24*60*60; // simulation horizon in seconds

	private RandomGenerator random;
	
	private Options options;
	private CiCoData cicoData;
	private Timetable timetable;
	private Map<Journey, ArrivalProcess> arrivalProcesses;
	private RollingStockComposerBasic rcomposer;
	
	private Map<Journey, Set<ScheduledTrip>> journeyToTrips;
	private Map<Journey, Integer> journeyCounts;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public ParallelSimModel(
			Timetable timetable, 
			RealisationData rdata, 
			Options options) {
		Long seed = options.getSeed();
		if (seed == null)
			random = new MersenneTwister();
		else
			random = new MersenneTwister(options.getSeed());
		this.options = options;
		this.timetable = timetable;
		journeyToTrips = new ConcurrentHashMap<>();
		journeyCounts = new ConcurrentHashMap<>();
		
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
		
		// estimate mismatch probabilities
		log.info("Begin estimating mismatch probabilities...");
		rcomposer = new RollingStockComposerBasic(timetable, rdata, random.nextLong());
		rcomposer = rcomposer.decreaseMismatches(1.0);
		log.info("...Finish estimating mismatch probabilities");
//		log.info("Exporting probabilities...");
//		try {
//			rcomposer.exportProbabilities("data/materieelplan/mismatch_probs.csv");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		log.info("... Finish exporting mismatch probabilities");
		
		// estimate arrival processes
		log.info("Begin estimating arrival processes...");
		arrivalProcesses = estimateArrivalProcesses(cicoData);
		log.info("...Finish estimating arrival processes");
	}
	
	public ParallelReport start(int iterations) {
		if (iterations < 1)
			throw new IllegalArgumentException("Number of iterations must be > 0");
		// start parallel computing
		log.info("Start parallel computing...");
//		int threads = Runtime.getRuntime().availableProcessors();
//		threads /= 2;
		int threads = options.getNumberOfProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);
		Set<Future<Report>> futures = new HashSet<>();
		Set<Report> reports = Collections.newSetFromMap(new ConcurrentHashMap<>());
		for (int i = 0; i < iterations; i++) {
			Callable<Report> callable = new SimCallable(new RollingStockComposerBasic(rcomposer, random.nextLong()));
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
		
		// clean up journey list
		cleanJourneyMap(iterations);
		
		return new ParallelReport(reports, timetable.getAllTrips(options.getDayOfWeek()), journeyToTrips);
//		return new ParallelReport(reports, timetable.getAllTrips(options.getDayOfWeek()));
	}
	
	private void cleanJourneyMap(int iterations) {
		for (Entry<Journey, Integer> entry : journeyCounts.entrySet()) {
			Journey journey = entry.getKey();
			int count = entry.getValue();
			if (count < 10*iterations)
				journeyToTrips.remove(journey);
		}
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
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, horizon, options.getSegmentWidth()*60, random.nextLong());
//			PiecewiseConstantProcess arrivalProcess = new PiecewiseConstantProcess(passengers, options.getSegmentWidth()*60, random.nextLong());
			resultMap.put(journey, arrivalProcess);
			double lambda = arrivalProcess.getLambdaUpperBound();
			if (lambda > maxLambda)
				maxLambda = lambda;
		}
		return resultMap;
	}
	
	synchronized void addTripToJourney(Journey journey, ScheduledTrip trip) {
		Set<ScheduledTrip> set = journeyToTrips.get(journey);
		Integer count = journeyCounts.get(journey);
		if (set == null) {
			set = new LinkedHashSet<>();
			journeyToTrips.put(journey, set);
		}
		if (count == null) {
			count = 0;
		}
		journeyCounts.put(journey, count+1);
		set.add(trip);
	}
	
	private class SimCallable implements Callable<Report> {
		
		private RollingStockComposer rcomposer;
		
		public SimCallable(RollingStockComposer rcomposer) {
			this.rcomposer = rcomposer;
		}

		@Override
		public Report call() throws Exception {
			Timetable realizedTimetable = new Timetable(timetable);
			SimModel sim = new SimModel(
					realizedTimetable, 
					arrivalProcesses, 
					cicoData, 
					rcomposer, 
					options, 
					ParallelSimModel.this);
			Report report = sim.start();
			sim = null; // try to help garbage collection
			return report;
		}
		
	}
}
