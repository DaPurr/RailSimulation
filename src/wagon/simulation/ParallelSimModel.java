package wagon.simulation;

import java.io.IOException;
import java.util.*;
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
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public ParallelSimModel(
			Timetable timetable, 
			RealisationData rdata, 
			Options options) {
		random = new MersenneTwister(options.getSeed());
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
		rcomposer = new RollingStockComposerBasic(timetable, rdata, random.nextLong());
		log.info("...Finish estimating mismatch probabilities");
	}
	
	public ParallelReport start(int iterations) {
		if (iterations < 1)
			throw new IllegalArgumentException("Number of iterations must be > 0");
		// start parallel computing
		log.info("Start parallel computing...");
		int threads = Runtime.getRuntime().availableProcessors();
		threads /= 2;
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
		
		return new ParallelReport(reports, timetable.getAllTrips(options.getDayOfWeek()));
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
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, horizon, 5*60, random.nextLong());
//			ArrivalProcess arrivalProcess = new PiecewiseConstantProcess(passengers, 5*60, seed);
			resultMap.put(journey, arrivalProcess);
			double lambda = arrivalProcess.getLambdaUpperBound();
			if (lambda > maxLambda)
				maxLambda = lambda;
		}
		return resultMap;
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
					options);
			Report report = sim.start();
			sim = null; // try to help garbage collection
			return report;
		}
		
	}
}
