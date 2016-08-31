package wagon.simulation;

public class TripCounters {
	
	private double b;
	private double n;
	private final double seatCapacity;
	private final double normCapacity;
	
	public TripCounters(double b, double n, double normCapacity, double seatCapacity) {
		this.normCapacity = normCapacity;
		this.seatCapacity = seatCapacity;
		this.b = b;
		this.n = n;
	}
	
	public double getB() {
		return b;
	}
	
	public void setB(double b) {
		this.b = b;
	}

	public double getN() {
		return n;
	}
	
	public void setN(double n) {
		this.n = n;
	}
	
	public double getNormCapacity() {
		return normCapacity;
	}
	
	public double getSeatCapacity() {
		return seatCapacity;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "[b="+b + ", ";
		s += "n="+n + ", ";
		s += "normC="+ getNormCapacity() + ", ";
		s += "seatC="+ getSeatCapacity();
		s += "]";
		
		return s;
	}

}
