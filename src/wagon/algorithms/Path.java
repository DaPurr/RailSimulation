package wagon.algorithms;

import java.util.*;

import wagon.network.WeightedEdge;

public class Path {
	private List<WeightedEdge> edges;
	
	public Path() {
		edges = new ArrayList<>();
	}
	
	public void addEdge(WeightedEdge e) {
		if (e == null)
			throw new NullPointerException("Edge cannot be null");
		edges.add(e);
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
//		boolean first = true;
		for (WeightedEdge e : edges) {
			s += e.toString() + System.lineSeparator();
		}
		return s + "]";
	}
}
