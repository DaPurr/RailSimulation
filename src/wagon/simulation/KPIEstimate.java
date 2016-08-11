package wagon.simulation;

public class KPIEstimate implements Comparable<KPIEstimate> {
	public final double mean;
	public final double std;
	
	public KPIEstimate(double mean, double std) {
		this.mean = mean;
		this.std = std;
	}
	
	@Override
	public String toString() {
		return mean + " (" + std + ")";
	}
	
	@Override
	public int hashCode() {
		return 13*Double.hashCode(mean) + 39*Double.hashCode(std);
	}

	@Override
	public int compareTo(KPIEstimate o) {
		int res1 = Double.compare(this.mean, o.mean);
		if (res1 != 0)
			return res1;
		return Double.compare(this.std, o.std);
	}
}