package wagon.simulation;

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.data.RealisationData;
import wagon.data.RollingStockComposer;
import wagon.data.RollingStockComposerBasic;
import wagon.infrastructure.Station;
import wagon.network.WeightedEdge;
import wagon.network.expanded.*;
import wagon.rollingstock.Composition;
import wagon.rollingstock.RollingStockUnit;
import wagon.timetable.*;

public class SimModel {
	
	public final static LocalTime BASE_TIME = LocalTime.of(0, 0, 0);

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	private Options options;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public SimModel(Timetable timetable, 
			EventActivityNetwork network, Options options) {
		
		log.setLevel(Level.INFO);
		
		// initialize basic variables
		eventQueue = new PriorityQueue<>();
		this.options = options;
		
		// sort out the CiCo data
		CiCoData cicoData = null;

		try {
			if (options.getPathToCiCoData() == null)
				throw new NullPointerException("Path to CiCo data cannot be null");
			cicoData = CiCoData.importRawData(
					options.getPathToCiCoData(), 
					"data/cico/omzettabel_stations.csv",  // hardcoded
					options);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (cicoData == null)
			throw new IllegalStateException("Something went wrong with passenger import");
		
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
		log.info("Passengers before removal: " + cicoData.getPassengers().size());
		log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
		log.info("Passengers remaining: " + passengers.size());
		cicoData.setPassengers(passengers);
		
		// estimate probabilities for rolling stock mismatches
		RealisationData rdata = null;
		try {
			rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (rdata == null)
			throw new IllegalStateException("Failed to load realisation data");
//		RollingStockComposer rcomposer = new RollingStockComposerBasic(timetable, rdata);
//		Timetable realizedTimetable = new Timetable(timetable);
//		generateMismatches(realizedTimetable, rcomposer);
		
		state = new SystemState(network, timetable, timetable, cicoData);
	}
	
	public Report start() {
		initialize();
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			event.process(state);
		}
		
		return new Report(state, options.getDayOfWeek());
	}
	
	private void initialize() {
		log.info("Begin processing passengers...");
		
		long count = 0;
		for (Entry<Journey, ArrivalProcess> entry : state.arrivalProcessEntries()) {
			Journey journey = entry.getKey();
			ArrivalProcess arrivalProcess = entry.getValue();
			count += generateJourneyArrivals(journey, arrivalProcess);
			if (count % 100 == 0)
				log.info("Processed events for " + count + " passengers...");
		}
		
		log.info("... Finish processing events for " + count + " passengers");
	}
	
	private int generateJourneyArrivals(Journey journey, ArrivalProcess arrivalProcess) {
		int horizon = 24*60*60;
		
		// generate arrivals
		List<Double> listArrivals = arrivalProcess.generateArrivalsFromProcess(horizon);
		
		// number of passengers for which we generated routes
		int countPassengersWithRoutes = 0;
		
		// transform arrival times (double) to arrivals (integers)
		for (double v : listArrivals) {
			int arrivalTime = (int) Math.floor(v);
			RouteGeneration routeGen = new LexicographicallyFirstGeneration(
					state.getNetwork(), 
					journey.origin.name(), 
					journey.destination.name(), 
					BASE_TIME.plusSeconds(arrivalTime));
			List<Path> paths = routeGen.generateRoutes();
			
			if (!hasNullRoute(paths)) {
				countPassengersWithRoutes++;
				RouteSelection routeSelector = new EarliestArrivalSelector();
				Path plannedRoute = routeSelector.selectPath(paths);
				processArrivalToEvents(plannedRoute);
			} else {
				log.fine("NULL ROUTE: " + BASE_TIME.plusSeconds(arrivalTime) + " " + journey.origin + " -> " + journey.destination);
			}
		}
		return countPassengersWithRoutes;
	}
	
	private void processArrivalToEvents(Path path) {
		ScheduledTrip boardingTrip = null;
		ScheduledTrip alightingTrip = null;
		
		for (WeightedEdge edge : path.getEdges()) {
			if (edge instanceof TripEdge) {
				TripEdge tEdge = (TripEdge) edge;
				alightingTrip = tEdge.trip();
				
				if (boardingTrip == null)
					boardingTrip = tEdge.trip();
			} else if (edge instanceof TransferEdge) {
				// we have transfered, so insert alighting and previous boarding event
				AlightingEvent alight = new AlightingEvent(alightingTrip, alightingTrip.arrivalTime());
				eventQueue.add(alight);
				
				BoardingEvent board = new BoardingEvent(boardingTrip, boardingTrip.departureTime());
				eventQueue.add(board);
				
				// reset boarding trip so that we insert the trip directly after the transfer
				boardingTrip = null;
			}
		}
		
		// add boarding and alighting
		BoardingEvent board = new BoardingEvent(boardingTrip, boardingTrip.departureTime());
		eventQueue.add(board);
		AlightingEvent alight = new AlightingEvent(alightingTrip, alightingTrip.arrivalTime());
		eventQueue.add(alight);
	}
	
	private boolean hasNullRoute(List<Path> paths) {
		for (int i = 0; i < paths.size(); i++) {
			Path path = paths.get(i);
			if (path == null)
				return true;
		}
		return false;
	}
	
	private void generateMismatches(Timetable timetable, RollingStockComposer composer) {
		Set<Composition> compositions = timetable.compositions();
		for (Composition comp : compositions) {
			boolean generate = true;
			Set<RollingStockUnit> currentPlannedUnits = new LinkedHashSet<>();
			Composition realizedComposition = null;
			SortedSet<ScheduledTrip> sortedTrips = timetable.getRoute(comp, options.getDayOfWeek());
			if (sortedTrips != null) {
				for (ScheduledTrip trip : sortedTrips) {
					if (!currentPlannedUnits.equals(trip.composition().getUnits())) {
						generate = true;
					}
					if (generate) {
						realizedComposition = composer.realizedComposition(trip.composition(), trip);
						generate = false;
						currentPlannedUnits = trip.composition().getUnits();
					}
					trip.setComposition(realizedComposition);
				}
			}
		}
	}
	
}
