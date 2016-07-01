package wagon.simulation;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class SimModel {

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	private Options options;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public SimModel(Timetable timetable, 
			EventActivityNetwork network, Options options) {
		
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
		Set<Station> availableStations = state.getTimetable().getStations();

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

		// initialize basic variables
		state = new SystemState(network, timetable, cicoData);
		eventQueue = new PriorityQueue<>();
		this.options = options;
	}
	
	public Report start() {
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			event.process(state);
		}
		
		return new Report(state);
	}
	
}
