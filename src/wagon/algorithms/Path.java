package wagon.algorithms;

import java.util.List;

import wagon.network.Node;
import wagon.network.WeightedEdge;

public interface Path {

	public double weight();
	public Node startNode();
	public Node endNode();
	public List<WeightedEdge> edges();
}
