package wagon.algorithms;

import java.util.ArrayList;
import java.util.List;

import wagon.network.Node;
import wagon.network.WeightedEdge;

public class DefaultPath implements Path {

	private double totalCost;
	private List<WeightedEdge> edges;
	
	DefaultPath() {
		edges = new ArrayList<>();
	}
	
	void addEdge(WeightedEdge e) {
		edges.add(e);
		totalCost += e.weight();
	}
	
	void removeEdge(WeightedEdge e) {
		edges.remove(e);
		totalCost -= e.weight();
	}
	
	@Override
	public double weight() {
		return totalCost;
	}

	@Override
	public Node startNode() {
		if (edges.isEmpty())
			return null;
		Node node = edges.get(0).source();
		return node;
	}

	@Override
	public Node endNode() {
		if (edges.isEmpty())
			return null;
		WeightedEdge edge = edges.get(edges.size()-1);
		Node node = edge.target();
		return node;
	}

	@Override
	public List<WeightedEdge> edges() {
		return new ArrayList<>(edges);
	}

}
