package wagon.network.expanded;

import java.time.*;
import java.util.*;
import java.util.logging.*;

import org.jgrapht.graph.DefaultDirectedGraph;

import com.google.common.collect.*;

import wagon.infrastructure.Station;
import wagon.network.Edge;
import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.rollingstock.ComfortNorm;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class EventActivityNetwork {
	
//	private final int timeLength = 16;

	private DefaultDirectedGraph<Node, WeightedEdge> graph;
	private Map<Edge, Double> capacities;
	
	// derived data
	Map<Station, TreeSet<DepartureNode>> departuresByStation;
	Map<Station, TreeSet<ArrivalNode>> arrivalsByStation;
	Map<Station, TreeSet<TransferNode>> transfersByStation;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	private EventActivityNetwork() {
		graph = new DefaultDirectedGraph<>(WeightedEdge.class);
		capacities = new LinkedHashMap<>();
		
		departuresByStation = new HashMap<>();
		arrivalsByStation = new HashMap<>();
		transfersByStation = new HashMap<>();
		
		log.setLevel(Level.OFF);
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
	 * Returns the weighted edge of this network where <code>u</code> is the
	 * source node and <code>v</code> its target.
	 * 
	 * @param u	source node
	 * @param v	target node
	 * @return	weighted edge with source <code>u</code> and target <code>v</code>
	 */
	public WeightedEdge getEdge(Node u, Node v) {
		return graph.getEdge(u, v);
	}
	
	/**
	 * Returns the set of all nodes present in <code>EventActivityNetwork</code>. Only the 
	 * <code>Node</code> objects in this <code>Set</code> are backed by the graph.
	 * 
	 * @return	set of all nodes
	 */
	public Set<Node> nodeSet() {
		return new LinkedHashSet<>(graph.vertexSet());
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
	public static EventActivityNetwork createTestNetwork1() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable 1...");
		Station station1 = new Station("Nwk");
		Station station2 = new Station("Cps");
		Station station3 = new Station("Rta");
		
		Composition comp1 = new Composition(1, TrainType.SGM,
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp2 = new Composition(2, TrainType.SGM, 
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp3 = new Composition(3, TrainType.SGM, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		
		ScheduledTrip sd1 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-11T06:47"), 
				LocalDateTime.parse("2016-04-11T06:50"), 
				station1, station2);
		ScheduledTrip sd2 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-11T07:17"), 
				LocalDateTime.parse("2016-04-11T07:20"), 
				station1, station2);
		ScheduledTrip sd3 = new ScheduledTrip(comp3, 
				LocalDateTime.parse("2016-04-11T07:47"), 
				LocalDateTime.parse("2016-04-11T07:50"), 
				station1, station2);
		
		ScheduledTrip sd4 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-11T06:50"), 
				LocalDateTime.parse("2016-04-11T06:53"), 
				station2, station3);
		ScheduledTrip sd5 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-11T07:20"), 
				LocalDateTime.parse("2016-04-11T07:23"), 
				station2, station3);
		ScheduledTrip sd6 = new ScheduledTrip(comp3, 
				LocalDateTime.parse("2016-04-11T07:50"), 
				LocalDateTime.parse("2016-04-11T07:53"), 
				station2, station3);
		
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
	 * @return	Returns a test network with two shortest paths
	 */
	public static EventActivityNetwork createTestNetwork2() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable 2...");
		Station stationA = new Station("A");
		Station stationB = new Station("B");
		Station stationC = new Station("C");
		
		Composition comp1 = new Composition(1, TrainType.SLT, 
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp2 = new Composition(2, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		
		ScheduledTrip trip1 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T10:53"), 
				LocalDateTime.parse("2016-04-19T11:00"), 
				stationA, stationB);
		ScheduledTrip trip2 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T11:00"), 
				LocalDateTime.parse("2016-04-19T11:30"), 
				stationB, stationC);
		ScheduledTrip trip3 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-19T11:10"), 
				LocalDateTime.parse("2016-04-19T11:30"), 
				stationB, stationC);
		
		Timetable timetable = new Timetable();
		timetable.addStation(stationA, trip1);
		timetable.addStation(stationB, trip2);
		timetable.addStation(stationB, trip3);
		
		network = createNetwork(timetable);
		network.log.info("...Finished constructing test timetable 2");
		
		return network;
	}
	
	/**
	 * @return	Returns a test network with a space loop.
	 */
	public static EventActivityNetwork createTestNetwork3() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable 3...");
		Station stationA = new Station("A");
		Station stationB = new Station("B");
		Station stationC = new Station("C");
		
		Composition comp1 = new Composition(1, TrainType.SLT, 
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp2 = new Composition(2, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		
		ScheduledTrip trip1 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T12:00"), 
				LocalDateTime.parse("2016-04-19T12:01"), 
				stationA, stationB);
		ScheduledTrip trip2 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T12:01"), 
				LocalDateTime.parse("2016-04-19T12:02"), 
				stationB, stationA);
		ScheduledTrip trip3 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-19T12:02"), 
				LocalDateTime.parse("2016-04-19T12:03"), 
				stationA, stationB);
		ScheduledTrip trip4 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-19T12:03"), 
				LocalDateTime.parse("2016-04-19T12:04"), 
				stationB, stationC);
		
		Timetable timetable = new Timetable();
		timetable.addStation(stationA, trip1);
		timetable.addStation(stationB, trip2);
		timetable.addStation(stationA, trip3);
		timetable.addStation(stationB, trip4);
		
		network = createNetwork(timetable);
		network.log.info("...Finished constructing test timetable 3");
		
		return network;
	}
	
	/**
	 * @return	Returns a test network with non-zero transfer
	 */
	public static EventActivityNetwork createTestNetwork4() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable 2...");
		Station stationA = new Station("A");
		Station stationB = new Station("B");
		Station stationC = new Station("C");
		Station stationD = new Station("D");
		
		Composition comp1 = new Composition(1, TrainType.SLT, 
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp2 = new Composition(2, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp3 = new Composition(3, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		
		ScheduledTrip trip1 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T10:53"), 
				LocalDateTime.parse("2016-04-19T11:00"), 
				stationA, stationB);
		ScheduledTrip trip2 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T11:05"), 
				LocalDateTime.parse("2016-04-19T11:30"), 
				stationB, stationC);
		ScheduledTrip trip3 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-19T11:10"), 
				LocalDateTime.parse("2016-04-19T11:30"), 
				stationB, stationC);
		ScheduledTrip trip4 = new ScheduledTrip(comp3, 
				LocalDateTime.parse("2016-04-19T11:35"), 
				LocalDateTime.parse("2016-04-19T11:40"), 
				stationC, stationD);
		
		Timetable timetable = new Timetable();
		timetable.addStation(stationA, trip1);
		timetable.addStation(stationB, trip2);
		timetable.addStation(stationB, trip3);
		timetable.addStation(stationC, trip4);
		
		network = createTransferNetwork(timetable, 1);
		network.log.info("...Finished constructing test timetable 2");
		
		return network;
	}
	
	/**
	 * @return	Returns a test network with two A-C paths: one 
	 * 			is earliest arrival, other minimum number of transfers
	 */
	public static EventActivityNetwork createTestNetwork5() {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin constructing test timetable 2...");
		Station stationA = new Station("A");
		Station stationB = new Station("B");
		Station stationC = new Station("C");
		
		Composition comp1 = new Composition(1, TrainType.SLT, 
				3, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp2 = new Composition(2, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		Composition comp3 = new Composition(3, TrainType.SLT, 
				6, 100, 20, 20, 20, 20, 20, 20, 20, ComfortNorm.C);
		
		ScheduledTrip trip1 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T11:05"), 
				LocalDateTime.parse("2016-04-19T11:19"), 
				stationA, stationB);
		ScheduledTrip trip2 = new ScheduledTrip(comp1, 
				LocalDateTime.parse("2016-04-19T11:19"), 
				LocalDateTime.parse("2016-04-19T12:10"), 
				stationB, stationC);
		ScheduledTrip trip3 = new ScheduledTrip(comp2, 
				LocalDateTime.parse("2016-04-19T11:06"), 
				LocalDateTime.parse("2016-04-19T11:07"), 
				stationA, stationB);
		ScheduledTrip trip4 = new ScheduledTrip(comp3, 
				LocalDateTime.parse("2016-04-19T11:18"), 
				LocalDateTime.parse("2016-04-19T12:00"), 
				stationB, stationC);
		
		Timetable timetable = new Timetable();
		timetable.addStation(stationA, trip1);
		timetable.addStation(stationB, trip2);
		timetable.addStation(stationB, trip3);
		timetable.addStation(stationC, trip4);
		
		network = createTransferNetwork(timetable, 1);
		network.log.info("...Finished constructing test timetable 2");
		
		return network;
	}
	
	private void addArrival(Station station, ArrivalNode u) {
		TreeSet<ArrivalNode> set = arrivalsByStation.get(station);
		if (set == null) {
			set = new TreeSet<>();
			set.add(u);
			arrivalsByStation.put(station, set);
		} else {
			set.add(u);
		}
		graph.addVertex(u);
	}
	
	private void addDeparture(Station station, DepartureNode u) {
		TreeSet<DepartureNode> set = departuresByStation.get(station);
		if (set == null) {
			set = new TreeSet<>();
			set.add(u);
			departuresByStation.put(station, set);
		} else {
			set.add(u);
		}
		graph.addVertex(u);
	}
	
	private void addTransfer(Station station, TransferNode u) {
		TreeSet<TransferNode> set = transfersByStation.get(station);
		if (set == null) {
			set = new TreeSet<>();
			set.add(u);
			transfersByStation.put(station, set);
		} else {
			set.add(u);
		}
		graph.addVertex(u);
	}
	
	public static EventActivityNetwork createTransferNetwork(
			Timetable timetable, 
			int transferTime) {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin creation of event-activity network with transfers...");
		
		// insert nodes into network
		network.log.info("Start inserting nodes...");
		long countArrivals = 0;
		long countDepartures = 0;
		long countTransfers = 0;
		long countWaitEdges = 0;
		long countTripEdges = 0;
		long countTransferEdges = 0;
		for (Composition comp : timetable.compositions()) {
			Iterator<ScheduledTrip> tripIter = timetable.getRoute(comp).iterator();
			ScheduledTrip previousTrip = tripIter.next();
			Triple<DepartureNode, ArrivalNode, TransferNode> previousTriple = addTripToNetwork(
					network, 
					previousTrip);
			countArrivals++; countDepartures++; countTransfers++;
			countWaitEdges++; countTripEdges++;
			while (tripIter.hasNext()) {
				ScheduledTrip currentTrip = tripIter.next();
				Triple<DepartureNode, ArrivalNode, TransferNode> currentTriple = addTripToNetwork(
						network, 
						currentTrip);
				countArrivals++; countDepartures++; countTransfers++;
				countWaitEdges++; countTripEdges++;
				if (countTripEdges % 100 == 0)
					network.log.info("Inserted " + countTripEdges + " trip edges...");
				
				// insert wait edge between consecutive trips from the same composition
				ArrivalNode prevArrivalNode = previousTriple.y;
				DepartureNode currDepartureNode = currentTriple.x;
				WaitEdge wEdge = new WaitEdge(
						prevArrivalNode, 
						currDepartureNode, 
						duration(
								prevArrivalNode.trip().arrivalTime(), 
								currDepartureNode.trip().departureTime()));
				network.graph.addEdge(prevArrivalNode, currDepartureNode, wEdge);
				countWaitEdges++;
				
				previousTriple = currentTriple;
			}
		}
		network.log.info("... Finish inserting nodes");
		
		network.log.info("Connect arrival nodes with compatible transfer nodes...");
		// connect arrival nodes with compatible transfer nodes
		for (Station station : timetable.getStations()) {
			for (ArrivalNode an : network.arrivalsByStation.get(station)) {
				// look for compatible transfer node
				TransferNode dummyTransfer = new TransferNode(
						an.trip().arrivalTime().plusMinutes(transferTime), 
						station);
				TransferNode tNode = network.transfersByStation.get(station).ceiling(dummyTransfer);
				if (tNode != null) {
					TransferEdge transEdge = new TransferEdge(
							an, tNode, 
							duration(an.trip().arrivalTime(), tNode.getTime()));
					network.graph.addEdge(an, tNode, transEdge);
					countTransferEdges++;
					if (countTransferEdges % 100 == 0)
						network.log.info("Added " + countTransferEdges + " transfer edges...");
				}
			}
		}
		network.log.info("... Finish connecting with compatible transfer nodes");
		
		network.log.info("Connect transfer nodes of the same station with each other...");
		// connect transfer nodes by means of wait edges
		for (Station station : timetable.getStations()) {
			PeekingIterator<TransferNode> transIter = Iterators
					.peekingIterator(network.transfersByStation.get(station).iterator());
			while (transIter.hasNext()) {
				TransferNode current = transIter.next();
				if (!transIter.hasNext())
					break;
				TransferNode next = transIter.peek();
				WaitEdge wEdge = new WaitEdge(
						current, 
						next, 
						duration(current.getTime(), next.getTime()));
				network.graph.addEdge(current, next, wEdge);
				countWaitEdges++;
				if (countWaitEdges % 100 == 0)
					network.log.info("Added " + countWaitEdges + " wait edges...");
			}
		}
		network.log.info("... Finish connecting transfer nodes");
		network.log.info("... Finish creating network");
		
		network.log.info("Event-activity network has:");
		network.log.info(countArrivals + " arrival nodes");
		network.log.info(countDepartures + " departure nodes");
		network.log.info(countTransfers + " transfer nodes");
		network.log.info(countTripEdges + " trip edges");
		network.log.info(countWaitEdges + " wait edges");
		network.log.info(countTransferEdges + " transfer edges");
		network.log.info("|V| = " + network.graph.vertexSet().size());
		network.log.info("|E| = " + network.graph.edgeSet().size());
		
		return network;
	}
	
	private static class Triple<X, Y, Z> {
		public final X x;
		public final Y y;
		public final Z z;
		public Triple(X x, Y y, Z z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	private static Triple<DepartureNode, ArrivalNode, TransferNode> addTripToNetwork(
			EventActivityNetwork network,
			ScheduledTrip trip) {
		
		TransferNode transfer = new TransferNode(
				trip.departureTime(), 
				trip.fromStation());
		DepartureNode departure = new DepartureNode(trip);
		ArrivalNode arrival = new ArrivalNode(trip);
		
		network.addArrival(trip.toStation(), arrival);
		network.addDeparture(trip.fromStation(), departure);
		network.addTransfer(trip.fromStation(), transfer);
		
		// insert trip edge
		TripEdge edge = new TripEdge(
				departure, 
				arrival, 
				trip, 
				duration(trip.departureTime(), trip.arrivalTime()));
		network.graph.addEdge(departure, arrival, edge);
		
		// insert wait edge from transfer node to departure node
		WaitEdge waitEdge = new WaitEdge(transfer, departure, 0.0);
		network.graph.addEdge(transfer, departure, waitEdge);
		
		return new Triple<>(departure, arrival, transfer);
	}
	
	/**
	 * Construct an event-activity network according to a <code>Timetable</code> object. 
	 * Does not (yet) take into account transfer time.
	 * 
	 * @param 	timetable	object containing the timetable which this network 
	 * 						is based on
	 * @return	event-activity network corresponding to <code>timetable</code>
	 */
	public static EventActivityNetwork createNetwork(Timetable timetable) {
		EventActivityNetwork network = new EventActivityNetwork();
		network.log.info("Begin import of timetable...");
		
		Set<Station> stations = new LinkedHashSet<>();
		
		// loop through the composition routes to create departure and arrival nodes for
		// each station
		network.log.info("Begin constructing trip edges...");
		int countTrips = 0;
		int countArrivals = 0;
		int countDepartures = 0;
		int counter = 0;
		for (Composition comp : timetable.compositions()) {
			for (ScheduledTrip trip : timetable.getRoute(comp)) {
				Station fromStation = trip.fromStation();
				Station toStation = trip.toStation();
				stations.add(fromStation);
				stations.add(toStation);
				
				DepartureNode dn = new DepartureNode(trip);
				ArrivalNode an = new ArrivalNode(trip);
				
				network.addArrival(toStation, an);
				network.addDeparture(fromStation, dn);
				
				WeightedEdge tripEdge = new TripEdge(dn, an, trip, 
						duration(trip.departureTime(), trip.arrivalTime()));
				network.graph.addEdge(dn, an, tripEdge);
				
				countDepartures++;
				countArrivals++;
				countTrips++;
				
				counter++;
				if (counter % 100 == 0)
					System.out.println("Processed " + counter + " trips...");
			}
		}
		network.log.info("...Finished constructing trip edges");
		network.log.info("Begin constructing wait edges...");
		
		// loop through each arrival and departure event and insert wait edges
		int countWaits = 0;
		for (Station station : stations) {
			SortedSet<EventNode> events = new TreeSet<>();
			if (network.departuresByStation.containsKey(station))
				events.addAll(network.departuresByStation.get(station));
			if (network.arrivalsByStation.containsKey(station))
				events.addAll(network.arrivalsByStation.get(station));
			EventNode prevEvent = null;
			for (EventNode event : events) {
				if (prevEvent == null) {
					prevEvent = event;
					continue;
				}
				LocalDateTime time1 = null;
				LocalDateTime time2 = null;
				if (prevEvent instanceof DepartureNode)
					time1 = prevEvent.trip().departureTime();
				else
					time1 = prevEvent.trip().arrivalTime();
				if (event instanceof DepartureNode)
					time2 = event.trip().departureTime();
				else
					time2 = event.trip().arrivalTime();
				int wait = duration(time1, time2);
				WeightedEdge waitEdge = new WaitEdge(prevEvent, event, wait);
				network.graph.addEdge(prevEvent, event, waitEdge);
				network.capacities.put(waitEdge, Double.MAX_VALUE);
				countWaits++;
				if (countWaits % 100 == 0)
					System.out.println("Inserted " + countWaits + " wait edges...");
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
	
	public TransferNode getNextTransferNode(Station station, LocalDateTime time) {
		TransferNode dummy = new TransferNode(time, station);
		return transfersByStation.get(station).ceiling(dummy);
	}
	
	/**
	 * Returns the departure node of this event-activity network corresponding to the station 
	 * with abbreviated name <code>name</code> and departure time <code>time</code>.
	 * <p>
	 * If there is no station with abbreviated name <code>name</code> this method returns 
	 * <code>null</code>. If there is no departure at the station that departs exactly at 
	 * <code>time</code>, this method returns the departure node corresponding to the 
	 * earliest departure after <code>time</code> or <code>null</code>, if no such 
	 * node exists.
	 * 
	 * @param name	abbreviated station name
	 * @param time	departure time
	 * @return	departure node
	 */
	public DepartureNode getStationDepartureNode(String name, LocalDateTime time) {
		if (name == null || time == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		Station station = new Station(name);
		NavigableSet<DepartureNode> set = departuresByStation.get(station);
		ScheduledTrip dummyTrip = new ScheduledTrip(
				new Composition(0, TrainType.VIRM, 
						0, 0, 0, 20, 20, 20, 20, 20, 20, ComfortNorm.C), 
				time, 
				time, 
				new Station(name), 
				new Station(name));
		DepartureNode returnNode = set.ceiling(new DepartureNode(dummyTrip));
		return returnNode;
	}
	
	/**
	 * Returns the arrival node of this event-activity network corresponding to the station 
	 * with abbreviated name <code>name</code> and arrival time <code>time</code>.
	 * <p>
	 * If there is no station with abbreviated name <code>name</code> this method returns 
	 * <code>null</code>. If there is no arrival at the station that arrives exactly at 
	 * <code>time</code>, this method returns the arrival node corresponding to the 
	 * earliest arrival before <code>time</code> or <code>null</code>, if no such 
	 * node exists.
	 * 
	 * @param name	abbreviated station name
	 * @param time	departure time
	 * @return	departure node
	 */
	public ArrivalNode getStationArrivalNode(String name, LocalDateTime time) {
		if (name == null || time == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		Station station = new Station(name);
		NavigableSet<ArrivalNode> set = arrivalsByStation.get(station);
		ScheduledTrip dummyTrip = new ScheduledTrip(
				new Composition(0, TrainType.VIRM, 
						0, 0, 0, 20, 20, 20, 20, 20, 20, ComfortNorm.C), 
				time, 
				time, 
				new Station(name), 
				new Station(name));
		ArrivalNode returnNode = set.floor(new ArrivalNode(dummyTrip));
		return returnNode;
	}
	
	/**
	 * @param station	railway station
	 * @return	departure nodes associated to <code>station</code>
	 */
	public Set<DepartureNode> getDeparturesByStation(Station station) {
		if (station == null)
			throw new IllegalArgumentException("Station cannot be null");
		return new LinkedHashSet<>(departuresByStation.get(station));
	}
	
	/**
	 * @param station	railway station
	 * @return	arrival nodes associated to <code>station</code>
	 */
	public Set<ArrivalNode> getArrivalsByStation(Station station) {
		if (station == null)
			throw new IllegalArgumentException("Station cannot be null");
		return new LinkedHashSet<>(arrivalsByStation.get(station));
	}
	
	public Set<EventNode> getEventsByStation(Station station) {
		if (station == null)
			throw new IllegalArgumentException("Station cannot be null");
		Set<DepartureNode> departures = getDeparturesByStation(station);
		Set<ArrivalNode> arrivals = getArrivalsByStation(station);
		Set<EventNode> events = new LinkedHashSet<>(departures);
		events.addAll(arrivals);
		return events;
	}
	
	public Set<TransferNode> getTransferNodesByStation(Station station) {
		if (station == null)
			throw new IllegalArgumentException("Station cannot be null");
		return new LinkedHashSet<>(transfersByStation.get(station));
	}
	
}
