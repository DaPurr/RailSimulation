package wagon.algorithms;

import java.util.*;

import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.network.expanded.EventActivityNetwork;

public class DijkstraShortestPath {

	private EventActivityNetwork network;
	
	/**
	 * Construct <code>DijkstraShortestPath</code> object for a given event-activity
	 * network.
	 * 
	 * @param network	event-activity network used to construct a shortest path
	 */
	public DijkstraShortestPath(EventActivityNetwork network) {
		this.network = network;
	}
	
	public Path shortestPath(Node start, Node stop) {
		
		Map<Node, Double> distance = new HashMap<>();
		
		// init queue
		PriorityQueue<DijkstraNode<Node>> queue = new PriorityQueue<>();

		// init distances
		for (Node node : network.nodeSet()) {
			distance.put(node, Double.POSITIVE_INFINITY);
		}

		// insert source
		queue.add(new DijkstraNode<Node>(start, 0.0));
		
		while (!queue.isEmpty()) {
			DijkstraNode<Node> dijkU = queue.poll();
			if (dijkU.e == stop)
				return constructPath(dijkU);
			double oldLabel = distance.get(dijkU.e);
			if (dijkU.weight < oldLabel) {
				distance.put(dijkU.e, dijkU.weight);
			}
			
			// traverse neighbors
			Set<WeightedEdge> outEdges = network.outgoingEdges(dijkU.e);
			for (WeightedEdge edge : outEdges) {
				Node v = edge.target();
				if (edge.source() != dijkU.e)
					throw new IllegalStateException("Inconsistency detected");
				if (dijkU.weight + edge.weight() < distance.get(v)) {
					queue.add(new DijkstraNode<Node>(v, dijkU.weight + edge.weight()));
					distance.put(v, dijkU.weight + edge.weight());
				}
			}
		}
		throw new IllegalStateException("Could not find sink node(s)");
	}
	
	private Path constructPath(DijkstraNode<Node> end) {
		DefaultPath path = new DefaultPath();
		DijkstraNode<Node> nextNode = end;
		while (nextNode != null) {
			path.addEdge(network.getEdge(nextNode.previous.e, nextNode.e));
			nextNode = nextNode.previous;
		}
		return path;
	}
	
	private class DijkstraNode<E> implements Comparable<DijkstraNode<E>> {
		
		private double weight;
		private E e;
		private DijkstraNode<E> previous;
		
		public DijkstraNode(E e, double weight) {
			this.weight = weight;
			this.e = e;
			this.previous = null;
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
