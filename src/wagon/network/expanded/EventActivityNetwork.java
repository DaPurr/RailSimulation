package wagon.network.expanded;

import java.time.*;
import java.util.*;
import java.util.logging.*;

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
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	private EventActivityNetwork() {
		graph = new DefaultDirectedGraph<>(WeightedEdge.class);
		capacities = new HashMap<>();
		//log.setLevel(Level.OFF);
	}
	
	/**
	 * Return the set of incoming edges of <code>node</code>.
	 * 
	 * @param node	candidate node
	 * @return	<code>Set</code> of incoming edges
	 */
	public Set<WeightedEdge> incomingEdges(Node node) {
		return graph.incomingEdgesOf(node);
	}
	
	/**
	 * Returns the set of all nodes present in <code>EventActivityNetwork</code>. Only the 
	 * <code>Node</code> objects in this <code>Set</code> are backed by the graph.
	 * 
	 * @return	set of all nodes
	 */
	public Set<Node> nodeSet() {
		return new HashSet<>(graph.vertexSet());
	}
	
	/**
	 * Return the set of outgoing edges of <code>node</code>.
	 * 
	 * @param node	candidate node
	 * @return	<code>Set</code> of outgoing edges
	 */
	public Set<WeightedEdge> outgoingEdges(Node node) {
		return graph.outgoingEdgesOf(node);
	}
	
	/**
	 * Creates an event-activity network, used explicitly for testing and debugging purposes.
	 * 
	 * @return	test network: Nwk -> Cps -> Rta, three consecutive trains
	 */
	public static EventActivityNetwork createTestNetwork() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable...");
		Station station1 = new Station("Nwk");
		Station station2 = new Station("Cps");
		Station station3 = new Station("Rta");
		
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
		network.log.info("...Constructed test timetable");
		
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
		network.log.info("Begin import of timetable...");
		
		// temporarily store departure and arrival nodes for each station
		Map<Station,SortedSet<DepartureNode>> departures = new HashMap<>();
		Map<Station,SortedSet<ArrivalNode>> arrivals = new HashMap<>();
		Set<Station> stations = new HashSet<>();
		
		// loop through the composition routes to create departure and arrival nodes for
		// each station
		network.log.info("Begin constructing trip edges...");
		int countTrips = 0;
		int countArrivals = 0;
		int countDepartures = 0;
		for (Composition comp : timetable.compositions()) {
			for (ScheduledTrip trip : timetable.getRoute(comp)) {
				Station fromStation = trip.fromStation();
				Station toStation = trip.toStation();
				stations.add(fromStation);
				stations.add(toStation);
				DepartureNode dn = new DepartureNode(fromStation, trip.departureTime());
				ArrivalNode an = new ArrivalNode(toStation, trip.arrivalTime());
				WeightedEdge tripEdge = new TripEdge(dn, an, trip, 
						duration(trip.departureTime(), trip.arrivalTime()));
				addEventNode(fromStation, dn, departures);
				countDepartures++;
				addEventNode(toStation, an, arrivals);
				countArrivals++;
				
				// add to network
				network.graph.addVertex(dn);
				network.graph.addVertex(an);
				network.graph.addEdge(dn, an, tripEdge);
				network.capacities.put(tripEdge, (double) trip.composition().capacity());
				countTrips++;
			}
		}
		network.log.info("...Finished constructing trip edges");
		network.log.info("Begin constructing wait edges...");
		
		// loop through each arrival and departure event and insert wait edges
		int countWaits = 0;
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
				WeightedEdge waitEdge = new WaitEdge(prevEvent, event, wait);
				network.graph.addEdge(prevEvent, event, waitEdge);
				network.capacities.put(waitEdge, Double.MAX_VALUE);
				countWaits++;
				prevEvent = event;
			}
		}
		network.log.info("...Finished constructing wait edges");
		network.log.info("...Finished importing timetable");
		
		// display stats
		network.log.info("Number of departure nodes: " + countDepartures);
		network.log.info("Number of arrival nodes: " + countArrivals);
		network.log.info("Number of trip edges: " + countTrips);
		network.log.info("Number of wait edges: " + countWaits);
		network.log.info("Total nr nodes: " + network.graph.vertexSet().size());
		network.log.info("Total nr edges: " + network.graph.edgeSet().size());
		
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
