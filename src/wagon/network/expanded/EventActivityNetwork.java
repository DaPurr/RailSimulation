package wagon.network.expanded;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

import wagon.infrastructure.Station;
import wagon.network.Edge;
import wagon.network.Node;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;
import wagon.timetable.ScheduledDeparture;
import wagon.timetable.Timetable;

public class EventActivityNetwork {

	private DefaultDirectedGraph<Node, Edge> graph;
	private Map<Edge, Double> capacities;
	
	private EventActivityNetwork() {
		graph = new DefaultDirectedGraph<>(Edge.class);
		capacities = new HashMap<>();
	}
	
	/**
	 * Creates an event-activity network, used explicitly for testing and debugging purposes.
	 * 
	 * @return	test network: Nwk -> Cps -> Rta, three trains
	 */
	public static EventActivityNetwork createTestNetwork() {
		EventActivityNetwork network = new EventActivityNetwork();
		Station station1 = new Station("Nwk", 1);
		Station station2 = new Station("Cps", 2);
		Station station3 = new Station("Rta", 3);
		
		Composition comp1 = new Composition(TrainType.SGM, 3, 100, 20);
		Composition comp2 = new Composition(TrainType.SGM, 3, 100, 20);
		Composition comp3 = new Composition(TrainType.SGM, 6, 100, 20);
		
		ScheduledDeparture sd1 = new ScheduledDeparture(comp1, LocalTime.parse("06:47"), station2);
		ScheduledDeparture sd2 = new ScheduledDeparture(comp2, LocalTime.parse("07:17"), station2);
		ScheduledDeparture sd3 = new ScheduledDeparture(comp3, LocalTime.parse("07:47"), station2);
		
		ScheduledDeparture sd4 = new ScheduledDeparture(comp1, LocalTime.parse("06:50"), station3);
		ScheduledDeparture sd5 = new ScheduledDeparture(comp2, LocalTime.parse("07:20"), station3);
		ScheduledDeparture sd6 = new ScheduledDeparture(comp3, LocalTime.parse("07:50"), station3);
		
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
		Map<Station,List<ArrivalNode>> arrivals = new HashMap<>();
		Map<Station,List<DepartureNode>> departures = new HashMap<>();
		Set<Station> stations = timetable.stations();
		
		// add event nodes
		for (Station fromStation : stations) {
			List<ScheduledDeparture> listDeps = timetable.departuresByStation(fromStation);
			List<DepartureNode> depNodes = new ArrayList<>();
			for (ScheduledDeparture dep : listDeps) {
				DepartureNode dn = new DepartureNode(fromStation, dep.time());
				depNodes.add(dn);
			}
			departures.put(fromStation, depNodes);
		}
		
		return network;
	}
	
	private static int duration(LocalTime time1, LocalTime time2) {
		return (int) Duration.between(time1, time1).toMinutes();
	}
	
}
