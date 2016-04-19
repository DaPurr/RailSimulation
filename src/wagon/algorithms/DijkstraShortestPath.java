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
		Map<EventNode, DijkstraNode<EventNode>> eventToDijkstra = new HashMap<>();
		
		// init queue
		PriorityQueue<DijkstraNode<EventNode>> queue = new PriorityQueue<>();

		// init distances
		for (Node node : network.nodeSet()) {
			distance.put(node, Double.POSITIVE_INFINITY);
		}

		// insert source
		DijkstraNode<EventNode> dijkSource = new DijkstraNode<EventNode>(start, 0.0);
		eventToDijkstra.put(start, dijkSource);
		queue.add(dijkSource);
		double minimumCost = Double.POSITIVE_INFINITY;
		
		while (!queue.isEmpty()) {
			DijkstraNode<EventNode> dijkU = queue.poll();
			eventToDijkstra.put(dijkU.e, dijkU);
			if ((dijkU.e instanceof ArrivalNode) && dijkU.e.trip().toStation().name().equals(to)) {
				minimumCost = dijkU.weight;
				List<DijkstraNode<EventNode>> endNodes = new ArrayList<>();
				endNodes.add(dijkU);
				for (DijkstraNode<EventNode> node : queue) {
					if (node.weight <= minimumCost &&
							(node.e instanceof ArrivalNode) &&
							node.e.trip().toStation().name().equals(to)) {
						endNodes.add(node);
					}
					else {
						break;
					}
				}
				long stopTime = System.nanoTime();
				double duration = (stopTime-startTime)*1e-9;
				log.info("...Finished calculating shortest path in: " + duration + " s");
				return constructPath(endNodes);
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
				double distV = distance.get(v);
				if (dijkU.weight + edge.weight() < distV) {
					List<DijkstraNode<EventNode>> nodes = new ArrayList<>();
					nodes.add(dijkU);
					DijkstraNode<EventNode> newNode = new DijkstraNode<EventNode>(v, nodes, dijkU.weight + edge.weight());
					queue.add(newNode);
					distance.put(v, dijkU.weight + edge.weight());
					eventToDijkstra.put(newNode.e, newNode);
				} else if (dijkU.weight + edge.weight() == distV) {
					DijkstraNode<EventNode> dijkV = eventToDijkstra.get(v);
					if (dijkV == null)
						throw new IllegalStateException("Could not find node v.");
					dijkV.previous.add(dijkU);
				}
			}
		}
		throw new IllegalStateException("Could not find sink node(s)");
	}
	
	private Path constructPath(List<DijkstraNode<EventNode>> endNodes) {
		List<Path> paths = new ArrayList<>();
		for (DijkstraNode<EventNode> node : endNodes) {
			List<Path> dfsPaths = depthFirstSearch(node);
			paths.addAll(dfsPaths);
		}
		Set<Path> setPaths = new HashSet<>();
		for (Path path : paths)
			setPaths.add(path);
		Path path = paths.get(0);
		System.out.println(paths);
		return path;
	}
	
	// employ depth-first search to find all shortest paths
	private List<Path> depthFirstSearch(DijkstraNode<EventNode> u) {
//		List<Path> paths = new ArrayList<>();
//		Stack<DijkstraNode<EventNode>> stack = new Stack<>();
//		List<WeightedEdge> reversePath = new ArrayList<>();
//		EventNode previous = u.e;
//		for (DijkstraNode<EventNode> node : u.previous)
//			stack.push(node);
//		while (!stack.isEmpty()) {
//			DijkstraNode<EventNode> v = stack.pop();
//			EventNode current = v.e;
//			WeightedEdge edge = network.getEdge(current, previous);
//			if (edge == null)
//				throw new IllegalStateException("Could not find edge (previous,current).");
//			reversePath.add(edge);
//			previous = v.e;
//			if (v.previous == null) {
//				DefaultPath path = new DefaultPath();
//				Collections.reverse(reversePath);
//				for (WeightedEdge e : reversePath)
//					path.addEdge(e);
//				paths.add(path);
//				reversePath = new ArrayList<>();
//				previous = u.e;
//				continue;
//			}
//			for (DijkstraNode<EventNode> node : v.previous)
//				stack.push(node);
//		}
//		return paths;
		
		List<Path> paths = new ArrayList<>();
		Stack<List<DijkstraNode<EventNode>>> stack = new Stack<>();
		
		// initialize DFS
		List<DijkstraNode<EventNode>> uList = new ArrayList<>();
		uList.add(u);
		stack.push(uList);
		
		while (!stack.isEmpty()) {
			List<DijkstraNode<EventNode>> list = stack.pop();
			List<DijkstraNode<EventNode>> neighbors = 
					list.get(list.size()-1).previous;
			if (neighbors == null) {
				Collections.reverse(list);
				DefaultPath path = new DefaultPath();
				for (int i = 0; i < list.size()-1; i++) {
					EventNode s = list.get(i).e;
					EventNode t = list.get(i+1).e;
					WeightedEdge edge = network.getEdge(s, t);
					path.addEdge(edge);
				}
				paths.add(path);
				continue;
			}
			for (DijkstraNode<EventNode> node : neighbors) {
				List<DijkstraNode<EventNode>> listCopy = new ArrayList<>(list);
				listCopy.add(node);
				stack.push(listCopy);
			}
		}
		return paths;
	}
	
	private class DijkstraNode<E> implements Comparable<DijkstraNode<E>> {
		
		private double weight;
		private E e;
		private List<DijkstraNode<E>> previous;
		
		public DijkstraNode(E e, double weight) {
			this(e, null, weight);
		}
		
		public DijkstraNode(E e, List<DijkstraNode<E>> previous, double weight) {
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
