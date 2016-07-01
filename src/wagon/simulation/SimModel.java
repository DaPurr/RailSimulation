package wagon.simulation;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.network.WeightedEdge;
import wagon.network.expanded.*;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class SimModel {
	
	public final static LocalDateTime BASE_TIME = LocalDateTime.of(2016, 4, 11, 0, 0, 0);

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	private Options options;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public SimModel(Timetable timetable, 
			EventActivityNetwork network, Options options) {
		
		// initialize basic variables
		eventQueue = new PriorityQueue<>();
		this.options = options;
		
		// sort out the CiCo data
		CiCoData cicoData = null;

		try {
			if (options.getPathToRawCiCoData() == null)
				throw new NullPointerException("Path to CiCo data cannot be null");
			cicoData = CiCoData.importRawData(
					options.getPathToRawCiCoData(), 
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
		for (Passenger passenger : passengersToDelete)
			passengers.remove(passenger);
		log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
		cicoData.setPassengers(passengers);
		
		state = new SystemState(network, timetable, cicoData);
	}
	
	public Report start() {
		initialize();
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			event.process(state);
		}
		
		return new Report(state);
	}
	
	private void initialize() {
		log.info("Begin processing passengers...");
		
		long count = 0;
		for (Entry<Journey, ArrivalProcess> entry : state.arrivalProcessEntries()) {
			Journey journey = entry.getKey();
			ArrivalProcess arrivalProcess = entry.getValue();
			generateJourneyArrivals(journey, arrivalProcess);
			count++;
			if (count % 100 == 0)
				log.info("Processed events for " + count + " passengers...");
		}
		
		log.info("... Finish processing events for " + count + " passengers");
	}
	
	private void generateJourneyArrivals(Journey journey, ArrivalProcess arrivalProcess) {
		int horizon = 20*60*60; // don't generate after 20:00
		int time = arrivalProcess.generateArrival(0);
		while (time < horizon) {
			RouteGeneration routeGen = new LexicographicallyFirstGeneration(
					state.getNetwork(), 
					journey.origin.name(), 
					journey.destination.name(), 
					BASE_TIME.plusSeconds(time));
			List<Path> paths = routeGen.generateRoutes();
			RouteSelection routeSelector = new EarliestArrivalSelector();
			Path plannedRoute = routeSelector.selectPath(paths);
			processArrivalToEvents(plannedRoute);
			
			time = arrivalProcess.generateArrival(time);
		}
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
	}
	
}
