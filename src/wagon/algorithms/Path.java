package wagon.algorithms;

import java.time.LocalDateTime;
import java.util.List;

import wagon.network.Node;
import wagon.network.WeightedEdge;

public interface Path {

	public double weight();
	public Node startNode();
	public Node endNode();
	public List<WeightedEdge> edges();
	public LocalDateTime departureTime();
	public LocalDateTime arrivalTime();
	public int countTransfers();
	public String representation();
}
