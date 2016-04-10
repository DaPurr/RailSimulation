package wagon.network;

public abstract class WeightedEdge implements Edge {
	
	private double weight;
	
	public WeightedEdge(double weight) {
		this.weight = weight;
	}

	public double weight() {
		return weight;
	}
}
