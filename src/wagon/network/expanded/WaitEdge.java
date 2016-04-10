package wagon.network.expanded;

import wagon.network.WeightedEdge;

public class WaitEdge extends WeightedEdge {

	public WaitEdge(double weight) {
		super(weight);
	}
	
	@Override
	public String toString() {
		return "WAIT: " + weight();
	}

}
