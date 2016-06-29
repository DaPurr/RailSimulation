package wagon.network.expanded;

import wagon.network.Node;
import wagon.network.WeightedEdge;

public class WaitEdge extends WeightedEdge {

	public WaitEdge(Node source, Node target, double weight) {
		super(source, target, weight);
	}
	
	@Override
	public String toString() {
		return "WAIT: " + weight();
	}

}
