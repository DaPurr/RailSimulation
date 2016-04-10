package wagon.network.expanded;

import java.time.*;
import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import wagon.infrastructure.Station;
import wagon.network.Edge;
import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class EventActivityNetwork {

	private DefaultDirectedGraph<Node, WeightedEdge> graph;
	private Map<Edge, Double> capacities;
	
	private EventActivityNetwork() {
		graph = new DefaultDirectedGraph<>(WeightedEdge.class);
		capacities = new HashMap<>();
	}
	
	public Graph<Node, WeightedEdge> graph() {
		return graph;
	}
	
	/**
	 * Creates an event-activity network, used explicitly for testing and debugging purposes.
	 * 
	 * @return	test network: Nwk -> Cps -> Rta, three consecutive trains
	 */
	public static EventActivityNetwork createTestNetwork() {
		EventActivityNetwork network = new EventActivityNetwork();
		Station station1 = new Station("Nwk", 1);
		Station station2 = new Station("Cps", 2);
		Station station3 = new Station("Rta", 3);
		
		Composition comp1 = new Composition(1, TrainType.SGM, 3, 100, 20);
		Composition comp2 = new Composition(2, TrainType.SGM, 3, 100, 20);
		Composition comp3 = new Composition(3, TrainType.SGM, 6, 100, 20);
		
		ScheduledTrip sd1 = new ScheduledTrip(comp1, LocalDateTime.parse("2016-04-11T06:47"), 
				LocalDateTime.parse("2016-04-11T06:50"), station1, station2);
		ScheduledTrip sd2 = new ScheduledTrip(comp2, LocalDateTime.parse("2016-04-11T07:17"), 
				LocalDateTime.parse("2016-04-11T07:20"), station1, station2);
		ScheduledTrip sd3 = new ScheduledTrip(comp3, LocalDateTime.parse("2016-04-11T07:47"), 
				LocalDateTime.parse("2016-04-11T07:50"), station1, station2);
		
		ScheduledTrip sd4 = new ScheduledTrip(comp1, LocalDateTime.parse("2016-04-11T06:50"), 
				LocalDateTime.parse("2016-04-11T06:53"), station2, station3);
		ScheduledTrip sd5 = new ScheduledTrip(comp2, LocalDateTime.parse("2016-04-11T07:20"), 
				LocalDateTime.parse("2016-04-11T07:23"), station2, station3);
		ScheduledTrip sd6 = new ScheduledTrip(comp3, LocalDateTime.parse("2016-04-11T07:50"), 
				LocalDateTime.parse("2016-04-11T07:53"), station2, station3);
		
		Timetable timetable = new Timetable();
		timetable.addStation(station1, sd1);
		timetable.addStation(station1, sd2);
		timetable.addStation(station1, sd3);
		timetable.addStation(station2, sd4);
		timetable.addStation(station2, sd5);
		timetable.addStation(station2, sd6);
		
		network = createNetwork(timetable);
		
		return network;
	}
	
	/**
	 * Construct an event-activity network according to a <code>Timetable</code> object. 
	 * Does not (yet) take into account transfer time. TODO
	 * 
	 * @param 	timetable	object containing the timetable which this network 
	 * 						is based on
	 * @return	event-activity network corresponding to <code>timetable</code>
	 */
	public static EventActivityNetwork createNetwork(Timetable timetable) {
		EventActivityNetwork network = new EventActivityNetwork();
		
		// temporarily store departure and arrival nodes for each station
		Map<Station,SortedSet<DepartureNode>> departures = new HashMap<>();
		Map<Station,SortedSet<ArrivalNode>> arrivals = new HashMap<>();
		Set<Station> stations = new HashSet<>();
		
		// loop through the composition routes to create departure and arrival nodes for
		// each station
		for (Composition comp : timetable.compositions()) {
			for (ScheduledTrip trip : timetable.getRoute(comp)) {
				Station fromStation = trip.fromStation();
				Station toStation = trip.toStation();
				stations.add(fromStation);
				stations.add(toStation);
				DepartureNode dn = new DepartureNode(fromStation, trip.departureTime());
				ArrivalNode an = new ArrivalNode(toStation, trip.arrivalTime());
				WeightedEdge tripEdge = new TripEdge(trip, 
						duration(trip.departureTime(), trip.arrivalTime()));
				addEventNode(fromStation, dn, departures);
				addEventNode(toStation, an, arrivals);
				
				// add to network
				network.graph.addVertex(dn);
				network.graph.addVertex(an);
				network.graph.addEdge(dn, an, tripEdge);
				network.capacities.put(tripEdge, (double) trip.composition().capacity());
			}
		}
		
		// loop through each arrival and departure event and insert wait edges
		for (Station station : stations) {
			SortedSet<EventNode> events = new TreeSet<>();
			if (departures.containsKey(station))
				events.addAll(departures.get(station));
			if (arrivals.containsKey(station))
				events.addAll(arrivals.get(station));
			EventNode prevEvent = null;
			for (EventNode event : events) {
				if (prevEvent == null) {
					prevEvent = event;
					continue;
				}
				int wait = duration(prevEvent.time(), event.time());
				WeightedEdge waitEdge = new WaitEdge(wait);
				network.graph.addEdge(prevEvent, event, waitEdge);
				network.capacities.put(waitEdge, Double.MAX_VALUE);
				prevEvent = event;
			}
		}
		
		return network;
	}
	
	private static int duration(LocalDateTime time1, LocalDateTime time2) {
		Duration duration = Duration.between(time1, time2);
		int minutes = (int) duration.toMinutes();
		return minutes;
	}
	
	private static <T extends EventNode> void addEventNode(Station station, T eventNode, Map<Station,SortedSet<T>> events) {
		if (!events.containsKey(station)) {
			SortedSet<T> set = new TreeSet<>();
			set.add(eventNode);
			events.put(station, set);
		} else {
			SortedSet<T> set = events.get(station);
			set.add(eventNode);
		}
	}
	
}
