package wagon.network;

public abstract class WeightedEdge extends Edge {
	
	private double weight;
	
	public WeightedEdge(Node source, Node target, double weight) {
		super(source, target);
		this.weight = weight;
	}

	public double weight() {
		return weight;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof WeightedEdge))
			return false;
		WeightedEdge o = (WeightedEdge) other;
		return this.weight == o.weight &&
				super.equals(o);
	}
}
