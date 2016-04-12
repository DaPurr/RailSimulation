package wagon.network;

import wagon.network.expanded.EventNode;

public abstract class WeightedEdge extends Edge {
	
	private double weight;
	
	public WeightedEdge(EventNode source, EventNode target, double weight) {
		super(source, target);
		this.weight = weight;
	}

	public double weight() {
		return weight;
	}
}
