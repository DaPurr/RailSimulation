package wagon.simulation;

import java.util.*;

public class RealizedDataProcess implements ArrivalProcess {
	
	private List<Double> passengers;
	
	public RealizedDataProcess(Collection<Passenger> passengers) {
		this.passengers = new ArrayList<>();
		for (Passenger passenger : passengers) {
			double arrivalTime = passenger.getCheckInTime().toLocalTime().toSecondOfDay();
			this.passengers.add(arrivalTime);
		}
	}

	@Override
	public double generateArrival(double time, int horizon) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Double> generateArrivalsFromProcess(int horizon) {
		return new ArrayList<>(passengers);
	}

}
