package wagon.algorithms;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

import wagon.infrastructure.Station;
import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.network.expanded.ArrivalNode;
import wagon.network.expanded.EventActivityNetwork;
import wagon.network.expanded.TransferEdge;
import wagon.network.expanded.TransferNode;

public class BiCriterionDijkstra {
	
	private EventActivityNetwork network;
	private Criterion crit1;
	private Criterion crit2;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public BiCriterionDijkstra(
			EventActivityNetwork network, 
			Criterion crit1, 
			Criterion crit2) {
		this.network = network;
		this.crit1 = crit1;
		this.crit2 = crit2;
	}
	
	public Path lexicographicallyFirst(String from, String to, LocalDateTime departureTime) {
		if (from == null || to == null || departureTime == null)
			throw new IllegalArgumentException("Arguments cannot be null");
		TransferNode tNode = network.getNextTransferNode(new Station(from), departureTime);
		return lexicographicallyFirst(tNode, to);
	}
	
	public Path lexicographicallyFirst(Node source, String to) {
		log.info("Start bi-criterion Dijkstra algorithm...");
		long startTime = System.nanoTime();
		
		PriorityQueue<DijkstraNode> queue = new PriorityQueue<>();
		Map<Node, Label> mapLabel = new LinkedHashMap<>();
		Map<Node, DijkstraNode> toDijkstraNode = new LinkedHashMap<>();
		
		// insert source
		queue.add(new DijkstraNode(source, 0.0, 0.0));
		
		while (!queue.isEmpty()) {
			DijkstraNode currentNode = queue.poll();
			
			// if this node is an arrival node belonging to 'to', we're done
			if (currentNode.node instanceof ArrivalNode) {
				ArrivalNode arrival = (ArrivalNode) currentNode.node;
				if (arrival.trip().toStation().name().equalsIgnoreCase(to)) {
					Path path = createPath(currentNode);
					
					long endTime = System.nanoTime();
					double duration = (endTime - startTime)*1e-9;
					log.info("... bi-criterion Dijkstra terminated in: " + duration + " s");
					
					return path;
				}
			}
			
			Label currentMapLabel = getLabelFromMap(currentNode.node, mapLabel);
			
			// if we found an improvement...
			if (currentNode.label.compareTo(currentMapLabel) < 0) {
				// ... update label
				setLabelFromMap(currentNode.label, currentNode.node, mapLabel);
			}
			
			// traverse its neighbors
			Set<WeightedEdge> outEdges = network.outgoingEdges(currentNode.node);
			for (WeightedEdge edge : outEdges) {
				Node neighbor = edge.target();
				double nrTransfers = 0.0;
				double travelTime = edge.weight();
				if (edge instanceof TransferEdge)
					nrTransfers = 1.0;
				Label toAdd = createLabel(travelTime, nrTransfers);
				Label addedLabel = currentNode.label.add(toAdd);

				// if we found an improvement, add the node to the queue
				if (addedLabel.compareTo(getLabelFromMap(neighbor, mapLabel)) < 0) {
					DijkstraNode dijkstraNeighbor = new DijkstraNode(neighbor, currentNode, addedLabel);
					toDijkstraNode.put(neighbor, dijkstraNeighbor);
					setLabelFromMap(addedLabel, neighbor, mapLabel);
					queue.add(dijkstraNeighbor);
				}
			}
		}

		throw new IllegalStateException("Cannot find nodes belonging to destination!");
	}
	
	private Path createPath(DijkstraNode node) {
		if (node == null)
			throw new IllegalArgumentException("Node cannot be null");
		List<Node> nodeList = new ArrayList<>();
		DijkstraNode dijkNode = node;
		while (dijkNode != null) {
			nodeList.add(dijkNode.node);
			dijkNode = dijkNode.previous;
		}
		Collections.reverse(nodeList);
		
		// now retrieve the edges
		Path path = new Path();
		for (int i = 0; i < nodeList.size()-1; i++) {
			Node current = nodeList.get(i);
			Node next = nodeList.get(i+1);
			WeightedEdge edge = network.getEdge(current, next);
			if (edge == null)
				throw new NullPointerException("Cannot find edge (" + current + ", " + next + ")");
			path.addEdge(edge);
		}
		return path;
	}
	
	private Label createLabel(double travelTime, double nrTransfers) {
		double v1 = 0.0;
		double v2 = 0.0;
		
		if (crit1 == Criterion.DISTANCE)
			v1 = travelTime;
		else
			v1 = nrTransfers;
		
		if (crit2 == Criterion.DISTANCE)
			v2 = travelTime;
		else
			v2 = nrTransfers;
		return new Label(v1, v2);
	}
	
	private Label getLabelFromMap(Node n, Map<Node, Label> map) {
		Label label = map.get(n);
		if (label == null)
			return new Label(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		return label;
	}
	
	private void setLabelFromMap(Label label, Node n, Map<Node, Label> map) {
		map.put(n, label);
	}
	
	private static class DijkstraNode implements Comparable<DijkstraNode> {
		private Label label;
		private DijkstraNode previous;
		private Node node;
		
		public DijkstraNode(Node node, DijkstraNode previous, Label label) {
			this.node = node;
			this.previous = previous;
			this.label = label;
		}
		
		public DijkstraNode(Node node, double d1, double d2) {
			this.node = node;
			this.previous = null;
			label = new Label(d1, d2);
		}

		@Override
		public int compareTo(DijkstraNode o) {
			int res1 = label.compareTo(o.label);
			if (res1 != 0)
				return res1;
			int res2 = this.node.toString().compareTo(o.node.toString());
			return res2;
		}
	}
	
	private static class Label implements Comparable<Label> {

		private double[] criteria;
		
		public Label(double d1, double d2) {
			criteria = new double[2];
			criteria[0] = d1;
			criteria[1] = d2;
		}
		
		public double getFirstCriterion() {
			return criteria[0];
		}
		
		public double getSecondCriterion() {
			return criteria[1];
		}
		
		public Label add(Label label) {
			return new Label(criteria[0] + label.criteria[0], criteria[1] + label.criteria[1]);
		}
		
		@Override
		public int compareTo(Label o) {
			int res1 = Double.compare(getFirstCriterion(), o.getFirstCriterion());
			if (res1 != 0)
				return res1;
			int res2 = Double.compare(getSecondCriterion(), o.getSecondCriterion());
			return res2;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Label))
				return false;
			Label other = (Label) o;
			return Arrays.equals(this.criteria, other.criteria);
		}
		
		@Override
		public int hashCode() {
			return Arrays.hashCode(criteria);
		}
		
		@Override
		public String toString() {
			return "(" + criteria[0] + ", " + criteria[1] + ")";
		}
		
	}
}
