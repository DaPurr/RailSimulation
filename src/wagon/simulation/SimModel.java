package wagon.simulation;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import wagon.algorithms.*;
import wagon.data.CiCoData;
import wagon.network.WeightedEdge;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class SimModel {

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	
	public SimModel(Timetable timetable, EventActivityNetwork network) {
		state = new SystemState(network, timetable);
		eventQueue = new PriorityQueue<>();
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
			// import the passenger groups/routes
//			List<PassengerGroup> groups = importPassengerGroups("data/routes/test1.csv");
			CiCoData cicoData = CiCoData.importRawData("data/cico/ritten_20160112.csv",
					"data/cico/omzettabel_stations.csv");
			List<PassengerGroup> groups = cicoData.processPassengersIntoGroups(new RouteGeneration(state.getNetwork()));
			exportPassengerGroups("data/routes/groups_20160112.csv", groups);
			// process groups to events
//			processPassengerGroups(groups);
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
		
		File file = new File(file_name);
		BufferedWriter bw = new BufferedWriter(
				new FileWriter(file));
		for (PassengerGroup group : groups) {
			bw.write(group.getPath().representation() + ";" + group.size());
		}
		bw.close();
	}
	
	private void processPassengerGroups(List<PassengerGroup> groups) {
		Map<ScheduledTrip, List<PassengerGroup>> mapTripToGroups = 
				new HashMap<>();
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
