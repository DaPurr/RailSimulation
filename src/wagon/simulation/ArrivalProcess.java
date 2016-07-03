package wagon.simulation;

import java.util.List;

public interface ArrivalProcess {

	public double generateArrival(double time, int horizon);
	public List<Double> generateArrivalsFromProcess(int horizon);
}
