package wagon.simulation;

import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import wagon.algorithms.*;
import wagon.data.*;
import wagon.network.WeightedEdge;
import wagon.network.expanded.*;
import wagon.rollingstock.*;
import wagon.timetable.*;

public class SimModel {
	
	public final static LocalTime BASE_TIME = LocalTime.of(0, 0, 0);
	
	private ParallelSimModel parent;

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	private Options options;
	private Map<Journey, ArrivalProcess> arrivalProcesses;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public SimModel(
			Timetable timetable, 
			Map<Journey, ArrivalProcess> arrivalProcesses, 
			CiCoData cicoData, 
			RollingStockComposer rcomposer, 
			Options options) {
		
		log.setLevel(Level.INFO);
		
		// initialize basic variables
		eventQueue = new PriorityQueue<>();
		this.options = options;
		this.arrivalProcesses = arrivalProcesses;
		generateMismatches(timetable, rcomposer);
		
		EventActivityNetwork network = EventActivityNetwork.createTransferNetwork(
				timetable, 
				options.getDayOfWeek(), 
				options.getTransferTime());
		
		state = new SystemState(
				network, 
				timetable, 
				cicoData);
	}
	
	public SimModel(
			Timetable timetable, 
			Map<Journey, ArrivalProcess> arrivalProcesses, 
			CiCoData cicoData, 
			RollingStockComposer rcomposer, 
			Options options, 
			ParallelSimModel parent) {
		this(timetable, arrivalProcesses, cicoData, rcomposer, options);
		this.parent = parent;
	}
	
	public Report start() {
		initialize();
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			event.process(state);
		}
		Report report = new Report(state, options.getDayOfWeek());
		state = null; // help garbage collection
		return report;
	}
	
	private void initialize() {
		log.info("Begin processing passengers...");
		
		long count = 0;
		for (Entry<Journey, ArrivalProcess> entry : arrivalProcesses.entrySet()) {
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
				
				// add journey trips to parent
				boolean add = true;
				for (WeightedEdge edge : plannedRoute.getEdges()) {
					if (edge instanceof TripEdge) {
						TripEdge tEdge = (TripEdge) edge;
						if (add) {
							add = false;
							parent.addTripToJourney(journey, tEdge.trip());
						}
					} else if (edge instanceof TransferEdge)
						add = true;
				}
			} else {
				log.fine("NULL ROUTE: " + BASE_TIME.plusSeconds(arrivalTime) + " " + journey.origin + " -> " + journey.destination);
			}
		}
		return countPassengersWithRoutes;
	}
	
	private void processArrivalToEvents(Path path) {
		List<Event> events = new ArrayList<>();
		ScheduledTrip boardingTrip = null;
		ScheduledTrip alightingTrip = null;
		
		for (WeightedEdge edge : path.getEdges()) {
			if (edge instanceof TripEdge) {
				TripEdge tEdge = (TripEdge) edge;
				alightingTrip = tEdge.trip();
				
				if (boardingTrip == null) {
					boardingTrip = tEdge.trip();
					Event boarding = new BoardingEvent(tEdge.trip(), tEdge.trip().departureTime());
					events.add(boarding);
				}
			} else if (edge instanceof TransferEdge) {
				// we have transfered, so insert alighting and previous boarding event
				AlightingEvent alight = new AlightingEvent(alightingTrip, alightingTrip.arrivalTime());
				events.add(alight);
				
				// reset boarding trip so that we insert the trip directly after the transfer
				boardingTrip = null;
			}
		}
		
		// add boarding and alighting
		AlightingEvent alight = new AlightingEvent(alightingTrip, alightingTrip.arrivalTime());
		events.add(alight);
		
		// add events to queue
		for (Event event : events) {
			int size = eventQueue.size();
			eventQueue.add(event);
			if (eventQueue.size() != size+1)
				throw new IllegalStateException("Queue didn't increase by 1");
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
	
	private void generateMismatches(Timetable timetable, RollingStockComposer composer) {
		Set<TrainService> trainServices = timetable.getTrainServices();
		for (TrainService comp : trainServices) {
			boolean generate = true;
			Composition currentPlannedComposition = new Composition();
			TrainService realizedTrainService = null;
			SortedSet<ScheduledTrip> sortedTrips = timetable.getRoute(comp, options.getDayOfWeek());
			if (sortedTrips != null) {
				for (ScheduledTrip trip : sortedTrips) {
					if (!currentPlannedComposition.equals(trip.getTrainService().getComposition())) {
						generate = true;
					}
					if (generate) {
						realizedTrainService = composer.realizedComposition(trip.getTrainService(), trip);
						generate = false;
						currentPlannedComposition = trip.getTrainService().getComposition();
					}
					trip.setTrainService(realizedTrainService);
				}
			}
		}
	}
	
}
