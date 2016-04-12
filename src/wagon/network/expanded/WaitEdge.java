package wagon.network.expanded;

import wagon.network.WeightedEdge;

public class WaitEdge extends WeightedEdge {

	public WaitEdge(EventNode source, EventNode target, double weight) {
		super(source, target, weight);
	}
	
	@Override
	public String toString() {
		return "WAIT: " + weight();
	}

}
