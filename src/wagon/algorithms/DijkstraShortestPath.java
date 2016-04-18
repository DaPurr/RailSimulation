package wagon.algorithms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.network.expanded.ArrivalNode;
import wagon.network.expanded.EventActivityNetwork;
import wagon.network.expanded.EventNode;

public class DijkstraShortestPath {

	private EventActivityNetwork network;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Construct <code>DijkstraShortestPath</code> object for a given event-activity
	 * network.
	 * 
	 * This class implements Dijkstra's shortest path algorithm using Java's own PriorityQueue. 
	 * The algorithm that is used is one without decrease-key operations, where nodes are 
	 * inserted into the heap only when necessary, thus saving memory.
	 * 
	 * @param network	event-activity network used to construct a shortest path
	 */
	public DijkstraShortestPath(EventActivityNetwork network) {
		this.network = network;
	}
	
	/**
	 * Constructs a single shortest path from <code>start</code> to <code>stop</code>.
	 * 
	 * It is entirely possible to perform multiple queries using this method.
	 * 
	 * @param start	start node
	 * @param stop	stop node
	 * @return	shortest path from <code>start</code> to <code>stop</code>
	 */
	public Path earliestArrivalPath(String from, String to, LocalDateTime departureTime) {
		
		if (from == null || to == null || departureTime == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		log.info("Commence shortest-path calculation...");
		long startTime = System.nanoTime();
		
		// get departure node
		EventNode start = network.getStationDepartureNode(from, departureTime);
		if (start == null)
			throw new IllegalStateException("Cannot find a suitable departure node");
		
		Map<Node, Double> distance = new HashMap<>();
		
		// init queue
		PriorityQueue<DijkstraNode<EventNode>> queue = new PriorityQueue<>();

		// init distances
		for (Node node : network.nodeSet()) {
			distance.put(node, Double.POSITIVE_INFINITY);
		}

		// insert source
		queue.add(new DijkstraNode<EventNode>(start, 0.0));
		
		while (!queue.isEmpty()) {
			DijkstraNode<EventNode> dijkU = queue.poll();
			if ((dijkU.e instanceof ArrivalNode) && dijkU.e.trip().toStation().name().equals(to)) {
				long stopTime = System.nanoTime();
				double duration = (stopTime-startTime)*1e-9;
				log.info("...Finished calculating shortest path in: " + duration + " s");
				return constructPath(dijkU);
			}
			double oldLabel = distance.get(dijkU.e);
			if (dijkU.weight < oldLabel) {
				distance.put(dijkU.e, dijkU.weight);
			}
			
			// traverse neighbors
			Set<WeightedEdge> outEdges = network.outgoingEdges(dijkU.e);
			for (WeightedEdge edge : outEdges) {
				EventNode v = edge.target();
				if (edge.source() != dijkU.e)
					throw new IllegalStateException("Inconsistency detected");
				if (dijkU.weight + edge.weight() < distance.get(v)) {
					queue.add(new DijkstraNode<EventNode>(v, dijkU, dijkU.weight + edge.weight()));
					distance.put(v, dijkU.weight + edge.weight());
				}
			}
		}
		throw new IllegalStateException("Could not find sink node(s)");
	}
	
	private Path constructPath(DijkstraNode<EventNode> end) {
		DefaultPath path = new DefaultPath();
		List<WeightedEdge> reversePath = new ArrayList<>();
		DijkstraNode<EventNode> nextNode = end;
		while (nextNode.previous != null) {
			reversePath.add(network.getEdge(nextNode.previous.e, nextNode.e));
			nextNode = nextNode.previous;
		}
		Collections.reverse(reversePath);
		for (WeightedEdge edge : reversePath)
			path.addEdge(edge);
		return path;
	}
	
	private class DijkstraNode<E> implements Comparable<DijkstraNode<E>> {
		
		private double weight;
		private E e;
		private DijkstraNode<E> previous;
		
		public DijkstraNode(E e, double weight) {
			this(e, null, weight);
		}
		
		public DijkstraNode(E e, DijkstraNode<E> previous, double weight) {
			this.weight = weight;
			this.e = e;
			this.previous = previous;
		}

		@Override
		public int compareTo(DijkstraNode<E> o) {
			if (this.weight < o.weight)
				return -1;
			if (this.weight > o.weight)
				return 1;
			return 0;
		}
		
	}
}
