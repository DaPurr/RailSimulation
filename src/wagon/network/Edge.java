package wagon.network;

public abstract class Edge {
	
	private Node source;
	private Node target;
	
	public Edge(Node source, Node target) {
		this.source = source;
		this.target = target;
	}

	public Node source() {
		return source;
	}
	
	public Node target() {
		return target;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Edge))
			return false;
		Edge o = (Edge) other;
		return this.source.equals(o.source) &&
				this.target.equals(o.target);
	}
}
