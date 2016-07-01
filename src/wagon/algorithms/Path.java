package wagon.algorithms;

import java.util.*;

import wagon.network.WeightedEdge;
import wagon.network.expanded.TransferEdge;

public class Path {
	private List<WeightedEdge> edges;
	
	public Path() {
		edges = new ArrayList<>();
	}
	
	public Path(Path path) {
		this();
		for (WeightedEdge edge : path.edges)
			addEdge(edge);
	}
	
	public void addEdge(WeightedEdge e) {
		if (e == null)
			throw new NullPointerException("Edge cannot be null");
		edges.add(e);
	}
	
	public int numberOfTransfers() {
		int count = 0;
		for (WeightedEdge edge : edges) {
			if (edge instanceof TransferEdge)
				count++;
		}
		return count;
	}
	
	public double travelTime() {
		double sum = 0.0;
		for (WeightedEdge edge : edges)
			sum += edge.weight();
		return sum;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Path))
			return false;
		Path o = (Path) other;
		return this.edges.equals(o.edges);
	}
	
	@Override
	public int hashCode() {
		return edges.hashCode();
	}
	
	@Override
	public String toString() {
		String s = "[";
		for (WeightedEdge e : edges) {
			s += e.toString() + System.lineSeparator();
		}
		return s + "]";
	}
}
