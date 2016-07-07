package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.random.MersenneTwister;

public class HybridArrivalProcess implements ArrivalProcess {
	
	private List<Double> arrivals;
	private int segmentWidth;
	
	private MersenneTwister random;
	
	public HybridArrivalProcess(
			Collection<Passenger> passengers, 
			int segmentWidth) {
		this.segmentWidth = segmentWidth;
		
		// convert collection of passengers to arrivals
		for (Passenger passenger : passengers) {
			double arrivalTime = passenger
					.getCheckInTime().toLocalTime().toSecondOfDay();
			arrivalTime += random.nextDouble();
			arrivals.add(arrivalTime);
		}
		Collections.sort(arrivals);
	}

	@Override
	public double generateArrival(double time, int horizon) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Double> generateArrivalsFromProcess(int horizon) {
		// TODO Auto-generated method stub
		return null;
	}

}
