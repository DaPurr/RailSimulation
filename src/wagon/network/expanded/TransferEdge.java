package wagon.network.expanded;

import wagon.network.Node;
import wagon.network.WeightedEdge;

public class TransferEdge extends WeightedEdge {

	public TransferEdge(Node source, Node target, double weight) {
		super(source, target, weight);
	}

}
