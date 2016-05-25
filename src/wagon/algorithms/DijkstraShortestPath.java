package wagon.algorithms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

//import wagon.infrastructure.Station;
import wagon.network.*;
import wagon.network.expanded.*;

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
	 * @param from			abbreviated name of the origin station
	 * @param to			abbreviated name of the destination station
	 * @param departureTime	time of departure
	 * @return	shortest path from <code>start</code> to <code>stop</code>
	 */
	public List<Path> earliestArrivalPath(String from, String to, LocalDateTime departureTime) {
		DepartureNode start = network.getStationDepartureNode(from, departureTime);
		if (start == null)
			throw new IllegalStateException("Cannot find a suitable departure node");
		return earliestArrivalPath(start, to);
	}
	
	/**
	 * Constructs a single shortest path from <code>start</code> to <code>stop</code>.
	 * 
	 * It is possible to perform multiple queries using this method.
	 * 
	 * @param from			departure node
	 * @param to			abbreviated name of the destination station
	 * @return	earliest arrival path from <code>from</code> to <code>to</code>
	 */
	public List<Path> earliestArrivalPath(DepartureNode from, String to) {
		
		/* TODO: Now, we do forward searches until we find a path where the passenger
		 * arrives after his check-out. It is then possible for a train departing after this
		 * last departure to arrive earlier. So, we would also like to construct a latest 
		 * departure path so we know when to stop generating paths. */
		
		if (from == null || to == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		
		log.info("Commence shortest-path calculation...");
		long startTime = System.nanoTime();
		
		DepartureNode start = from;
		
		// sparse representation; only add when necessary
		Map<Node, Double> distance = new HashMap<>();
		Map<EventNode, DijkstraNode<EventNode>> eventToDijkstra = new HashMap<>();
		
		// init queue
		PriorityQueue<DijkstraNode<EventNode>> queue = new PriorityQueue<>();

		// insert source
		DijkstraNode<EventNode> dijkSource = new DijkstraNode<EventNode>(start, 0.0);
		eventToDijkstra.put(start, dijkSource);
		queue.add(dijkSource);
		
		// used to identify nodes with least cost
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
			Double oldLabel = distance.get(dijkU.e);
			if (oldLabel == null)
				oldLabel = Double.POSITIVE_INFINITY;
			if (dijkU.weight < oldLabel) {
				distance.put(dijkU.e, dijkU.weight);
			}
			
			// traverse neighbors
			Set<WeightedEdge> outEdges = network.outgoingEdges(dijkU.e);
			for (WeightedEdge edge : outEdges) {
				EventNode v = edge.target();
				if (edge.source() != dijkU.e)
					throw new IllegalStateException("Inconsistency detected");
				Double distV = distance.get(v);
				if (distV == null)
					distV = Double.POSITIVE_INFINITY;
				double edgeWeight = edge.weight();
				if (dijkU.weight + edgeWeight < distV) {
					List<DijkstraNode<EventNode>> nodes = new ArrayList<>();
					nodes.add(dijkU);
					DijkstraNode<EventNode> newNode = new DijkstraNode<EventNode>(v, nodes, dijkU.weight + edgeWeight);
					queue.add(newNode);
					distance.put(v, dijkU.weight + edgeWeight);
					eventToDijkstra.put(newNode.e, newNode);
				} else if (dijkU.weight + edgeWeight == distV) {
					DijkstraNode<EventNode> dijkV = eventToDijkstra.get(v);
					if (dijkV == null)
						throw new IllegalStateException("Could not find node v.");
					dijkV.previous.add(dijkU);
				}
			}
		}
		throw new IllegalStateException("Could not find sink node(s)");
	}
	
//	private List<Path> removeSpaceCycles(List<Path> paths) {
//		if (paths == null)
//			throw new IllegalArgumentException("Paths cannot be null");
//		
//		List<Path> newPaths = new ArrayList<>();
//		for (int i = 0; i < paths.size(); i++) {
//			if (!hasSpaceCycle(paths.get(i)))
//				newPaths.add(paths.get(i));
//		}
//		return newPaths;
//	}
	
//	private Station associatedStation(EventNode event) {
//		if (event instanceof ArrivalNode)
//			return event.trip().toStation();
//		if (event instanceof DepartureNode)
//			return event.trip().fromStation();
//		throw new IllegalArgumentException("Event can only be arrival or departure");
//	}
	
//	private boolean hasSpaceCycle(Path path) {
//		if (path == null)
//			throw new IllegalArgumentException("Path cannot be null");
//		Set<Station> visited = new HashSet<>();
//		List<WeightedEdge> edges = path.edges();
//		visited.add(edges.get(0).source().trip().fromStation());
//		for (int i = 0; i < edges.size(); i++) {
//			EventNode current = edges.get(i).source();
//			EventNode next = edges.get(i).target();
//			Station currStation = associatedStation(current);
//			Station nextStation = associatedStation(next);
//			if (!currStation.equals(nextStation) &&
//					!visited.add(nextStation))
//				return true;
//		}
//		return false;
//	}
	
//	private List<Path> filterLeastTransfers(List<Path> paths) {
//		List<Path> newPaths = new ArrayList<>();
//		int minTransfers = Integer.MAX_VALUE;
//		for (Path path : paths) {
//			int nrTransfers = countTransfers(path);
//			if (nrTransfers < minTransfers) {
//				newPaths = new ArrayList<>();
//				newPaths.add(path);
//				minTransfers = nrTransfers;
//			} else if (nrTransfers == minTransfers)
//				newPaths.add(path);
//		}
//		return newPaths;
//	}
	
//	private int countTransfers(Path path) {
//		List<WeightedEdge> edges = path.edges();
//		int count = 0;
//		
//		int prevID = -1;
//		for (WeightedEdge edge : edges) {
//			if (edge instanceof WaitEdge)
//				continue;
//			EventNode target = edge.target();
//			int targetID = target.trip().composition().id();
//			if (prevID == -1) {
//				prevID = targetID;
//				continue;
//			}
//			if (target instanceof ArrivalNode &&
//					targetID != prevID) {
//				count++;
//				prevID = targetID;
//			}
//		}
//		return count;
//	}
	
	private List<Path> constructPath(List<DijkstraNode<EventNode>> endNodes) {
		List<Path> paths = new ArrayList<>();
		for (DijkstraNode<EventNode> node : endNodes) {
			List<Path> dfsPaths = depthFirstSearch(node);
			paths.addAll(dfsPaths);
		}
//		paths = removeSpaceCycles(paths);
		return paths;
	}
	
	// employ depth-first search to find all shortest paths
	private List<Path> depthFirstSearch(DijkstraNode<EventNode> u) {
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
