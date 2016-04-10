package wagon.network.expanded;

import java.time.Duration;
import java.time.LocalDateTime;
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
import wagon.timetable.ScheduledTrip;
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
		
		ScheduledTrip sd1 = new ScheduledTrip(comp1, LocalDateTime.parse("06:47"), 
				LocalDateTime.parse("06:50"), station1, station2);
		ScheduledTrip sd2 = new ScheduledTrip(comp2, LocalDateTime.parse("07:17"), 
				LocalDateTime.parse("07:20"), station1, station2);
		ScheduledTrip sd3 = new ScheduledTrip(comp3, LocalDateTime.parse("07:47"), 
				LocalDateTime.parse("07:50"), station1, station2);
		
		ScheduledTrip sd4 = new ScheduledTrip(comp1, LocalDateTime.parse("06:50"), 
				LocalDateTime.parse("06:53"), station2, station3);
		ScheduledTrip sd5 = new ScheduledTrip(comp2, LocalDateTime.parse("07:20"), 
				LocalDateTime.parse("07:23"), station2, station3);
		ScheduledTrip sd6 = new ScheduledTrip(comp3, LocalDateTime.parse("07:50"), 
				LocalDateTime.parse("07:53"), station2, station3);
		
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
		
		return network;
	}
	
	private static int duration(LocalDateTime time1, LocalDateTime time2) {
		return (int) Duration.between(time1, time1).toMinutes();
	}
	
}
