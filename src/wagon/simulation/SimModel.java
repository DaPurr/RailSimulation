package wagon.simulation;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.network.WeightedEdge;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class SimModel {

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	private Options options;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public SimModel(Timetable timetable, 
			EventActivityNetwork network, 
			Options options) {
		state = new SystemState(network, timetable);
		eventQueue = new PriorityQueue<>();
		this.options = options;
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
		try {
			if (options.getPathToProcessedGroupsData() == null &&
					options.getPathToRawCiCoData() == null)
				throw new IllegalStateException("You need to either import raw data or"
						+ "processed passenger group data.");
			List<PassengerGroup> groups = null;
			if (options.getPathToProcessedGroupsData() != null) {
				groups = importPassengerGroups(options.getPathToProcessedGroupsData());
			} else {
				CiCoData cicoData = CiCoData
						.importRawData(	options.getPathToRawCiCoData(),
										"data/cico/omzettabel_stations.csv"); // hardcoded
				Set<Passenger> passengers = cicoData.getPassengers();
				Set<Passenger> passengersToDelete = new LinkedHashSet<>();
				Set<Station> availableStations = state.getTimetable().getStations();
				
				// remove passengers with origin or destination not in timetable
				for (Passenger passenger : passengers) {
					Station from = passenger.getFromStation();
					Station to = passenger.getToStation();
					if (!availableStations.contains(from) || 
							!availableStations.contains(to)) {
						passengersToDelete.add(passenger);
					}
				}
				for (Passenger passenger : passengersToDelete)
					passengers.remove(passenger);
				log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
				cicoData.setPassengers(passengers);
				groups = cicoData.processPassengersIntoGroups(new RouteGeneration(state.getNetwork()));
//				String[] parts = options.getPathToRawCiCoData().split("/");
//				String tempie = parts[parts.length-1];
//				String[] parts2 = tempie.split("\\.");
//				String fileName = parts[0] + "/" + parts2[0] + "_groups.csv";
				String pathName = options.getPathToRawCiCoData();
				String fileName = pathName.substring(0, pathName.length()-4) + "_groups.csv";
				exportPassengerGroups(fileName, groups);
			}
			
			// process passenger groups into events
			processPassengerGroups(groups);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<PassengerGroup> importPassengerGroups(String file_name) throws IOException {
		List<PassengerGroup> groups = new ArrayList<>();
		BufferedReader br = new BufferedReader(
				new FileReader(new File(file_name)));
		String line = br.readLine(); // throw away headers
		line = br.readLine();
		while (line != null) {
			String[] parts = line.split(";");
			DefaultPath path = state.getNetwork().textToPath(parts[0]);
			double groupSize = Double.valueOf(parts[1]);
			PassengerGroup group = new PassengerGroup(path, groupSize);
			groups.add(group);
			line = br.readLine();
		}
		br.close();
		
		return groups;
	}
	
	private void exportPassengerGroups(String file_name, 
			Collection<PassengerGroup> groups) throws IOException {
		if (!file_name.matches(".*\\.csv"))
			throw new IllegalArgumentException("File needs to be CSV format.");
		
		log.info("Begin exporting passenger groups ...");
		File file = new File(file_name);
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(file));
		long counter = 0;
		for (PassengerGroup group : groups) {
			counter++;
			bw.write(group.getPath().representation() + ";" + group.size() + System.lineSeparator());
			if (counter % 100 == 0)
				System.out.println("... Finish writing " + counter + " passenger groups to disk");
		}
		bw.close();
	}
	
	private void processPassengerGroups(List<PassengerGroup> groups) {
		Map<ScheduledTrip, List<PassengerGroup>> mapTripToGroups = 
				new LinkedHashMap<>();
		// make lists of groups according to boarding trip
		for (PassengerGroup group : groups) {
			List<WeightedEdge> edges = group.getPath().edges();
			ScheduledTrip trip = edges.get(0).source().trip();
			List<PassengerGroup> groupList = mapTripToGroups.get(trip);
			if (groupList == null) {
				groupList = new ArrayList<>();
				mapTripToGroups.put(trip, groupList);
			}
			groupList.add(group);
		}
		
		// add events to queue
		for (Entry<ScheduledTrip, List<PassengerGroup>> entry : mapTripToGroups.entrySet()) {
			ScheduledTrip trip = entry.getKey();
			List<PassengerGroup> entryGroups = entry.getValue();
			BoardingEvent event = new BoardingEvent(trip, entryGroups, trip.departureTime());
			eventQueue.add(event);
		}
	}
	
}
