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
import wagon.rollingstock.TrainService;
import wagon.timetable.*;

public class ParallelSimModel {
	
	private final int horizon = 24*60*60; // simulation horizon in seconds

	private RandomGenerator random;
	
	private Options options;
	private CiCoData cicoData;
	private Timetable timetable;
	private Map<Journey, ArrivalProcess> arrivalProcesses;
	private RollingStockComposerBasic rcomposer;
	
	// for worst OD-pairs
	private Map<Journey, Set<Trip>> journeyToTrips;
	private Map<Journey, Integer> journeyCounts;
	
	// for worst origins
	private Map<Station, Set<Trip>> originToTrips;
	private Map<Station, Integer> originCounts;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public ParallelSimModel(
			Timetable timetable, 
			RealisationData rdata, 
			Map<Journey, ArrivalProcess> arrivalProcesses, 
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
		
		originToTrips = new ConcurrentHashMap<>();
		originCounts = new ConcurrentHashMap<>();
		
		this.arrivalProcesses = arrivalProcesses;
		
		// estimate mismatch probabilities
		log.info("Begin estimating mismatch probabilities...");
		rcomposer = new RollingStockComposerBasic(timetable, rdata, random.nextLong());
		rcomposer = rcomposer.decreaseMismatches(options.getPhi());
		log.info("...Finish estimating mismatch probabilities");
		log.info("Exporting probabilities...");
		try {
			rcomposer.exportProbabilities("data/materieelplan/mismatch_probs.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("... Finish exporting mismatch probabilities");
		
		// estimate train cancellation probability psi
		double psi = estimatePsi(rdata);
		options.setPsi(psi);
	}
	
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
		
		originToTrips = new ConcurrentHashMap<>();
		originCounts = new ConcurrentHashMap<>();
		
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
		rcomposer = rcomposer.decreaseMismatches(options.getPhi());
		log.info("...Finish estimating mismatch probabilities");
		log.info("Exporting probabilities...");
		try {
			rcomposer.exportProbabilities("data/materieelplan/mismatch_probs.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("... Finish exporting mismatch probabilities");
		
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
		
		return new ParallelReport(reports, timetable.getAllTrips(options.getDayOfWeek()), journeyToTrips, originToTrips);
	}
	
	private void cleanJourneyMap(int iterations) {
		for (Entry<Journey, Integer> entry : journeyCounts.entrySet()) {
			Journey journey = entry.getKey();
			int count = entry.getValue();
			if (count < 15*iterations)
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
		for (Journey journey : map.keySet()) {
			Collection<Passenger> passengers = map.get(journey);
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, horizon, options.getSegmentWidth()*60, random.nextLong());
//			PiecewiseConstantProcess arrivalProcess = new PiecewiseConstantProcess(passengers, options.getSegmentWidth()*60, random.nextLong());
//			RealizedDataProcess arrivalProcess = new RealizedDataProcess(passengers);
			resultMap.put(journey, arrivalProcess);
		}
		return resultMap;
	}
	
	private double estimatePsi(RealisationData rdata) {
		double psi = 0.0;
		int countTotal = 0;
		for (Entry<Integer, SortedSet<RealisationDataEntry>> entry : rdata.entrySet()) {
			RealisationDataEntry firstEntry = entry.getValue().first();
			countTotal++;
			if (firstEntry.getRealizedDepartureTime() == RealisationData.DUMMY &&
					firstEntry.getRealizedArrivalTime() == RealisationData.DUMMY) {
				psi++;
			}
		}
		psi /= countTotal;
		return psi;
	}
	
	synchronized void addTripToJourney(Journey journey, Trip trip) {
		Set<Trip> set = journeyToTrips.get(journey);
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
	
	synchronized void addTripToOrigin(Station station, Trip trip) {
		Set<Trip> set = originToTrips.get(station);
		Integer count = originCounts.get(station);
		if (set == null) {
			set = new LinkedHashSet<>();
			originToTrips.put(station, set);
		}
		if (count == null) {
			count = 0;
		}
		originCounts.put(station, count+1);
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
			Set<Integer> canceledTrains = new LinkedHashSet<>();
			double psi = options.getPsi();
			for (TrainService service : realizedTimetable.getTrainServices()) {
				double r = random.nextDouble();
				if (r <= psi)
					canceledTrains.add(service.id());
			}
			realizedTimetable.cancelTrain(canceledTrains);
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
