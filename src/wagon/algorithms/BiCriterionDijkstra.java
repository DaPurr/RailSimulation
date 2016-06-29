package wagon.algorithms;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.logging.Logger;

import wagon.network.Node;
import wagon.network.expanded.EventActivityNetwork;

public class BiCriterionDijkstra {
	
	private EventActivityNetwork network;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public BiCriterionDijkstra(
			EventActivityNetwork network, 
			Criterion crit1, 
			Criterion crit2) {
		this.network = network;
	}
	
	public Path lexicographicallyFirst(String from, String to, LocalDateTime departureTime) {
		
	}
	
	public Path lexicographicallyFirst(Node departure, String to) {
		log.info("Initiate bi-criterion Dijkstra algorithm...");
		
		PriorityQueue<DijkstraNode> queue = new PriorityQueue<>();
	}
	
	private class DijkstraNode implements Comparable<DijkstraNode> {
		private Label label;
		private DijkstraNode previous;
		private Node node;
		
		public DijkstraNode(Node node, DijkstraNode previous, double d1, double d2) {
			this.node = node;
			this.previous = previous;
			label = new Label(d1, d2);
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
	
	private class Label implements Comparable<Label> {

		private double[] criteria;
		
		public Label() {
			criteria = new double[2];
		}
		
		public Label(double d1, double d2) {
			criteria = new double[2];
			criteria[0] = d1;
			criteria[1] = d2;
		}
		
		public void setFirstCriterion(double v) {
			criteria[0] = v;
		}
		
		public void setSecondCriterion(double v) {
			criteria[1] = v;
		}
		
		public double getFirstCriterion() {
			return criteria[0];
		}
		
		public double getSecondCriterion() {
			return criteria[1];
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
		
	}
}
