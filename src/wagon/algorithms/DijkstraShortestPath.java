package wagon.algorithms;

import java.util.*;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import wagon.network.Node;
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
		// initialize algorithm
		FibonacciHeap<Node> unvisited = new FibonacciHeap<>();
		for (Node node : network.nodeSet()) {
			FibonacciHeapNode<Node> heapNode = new FibonacciHeapNode<Node>(node);
		}
		
		return null;
	}
	
	
}
