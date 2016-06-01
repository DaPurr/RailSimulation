package wagon.simulation;

import java.io.IOException;
import java.util.*;

import wagon.timetable.ScheduledTrip;

public class Report {

	private SystemState state;
	
	public Report(SystemState state) {
		this.state = state;
	}
	
	public String summary() {
		String s = "";
		Set<ScheduledTrip> trips = state.getTimetable().getAllTrips();
		s += "KPI_{old}=" + calculateKPIOld(trips) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(trips);
		return s;
	}
	
	public void exportToFile(String file_name) throws IOException {
		
	}
	
	/**
	 * Given a collection of trips, calculate the old KPI.
	 * 
	 * @param trips	the collection of trips
	 * @return	the old KPI for transport capacity
	 */
	public double calculateKPIOld(Collection<ScheduledTrip> trips) {
		double numerator = 0.0;
		double denominator = 0.0;
		for (ScheduledTrip trip : trips) {
			Counter counterN = state.getTripCounterN(trip);
			if (counterN == null)
				throw new IllegalArgumentException("Counters for trip cannot be found.");
			double countN = counterN.getValue();
			int normCapacity = trip.composition().normCapacity();
			numerator += countN*Math.min(normCapacity/countN, 1);
			denominator += countN;
		}
		return numerator/denominator;
	}
	
	/**
	 * Given a collection of trips, calculate the new KPI.
	 * 
	 * @param trips	the collection of trips
	 * @return	the new KPI for transport capacity
	 */
	public double calculateKPINew(Collection<ScheduledTrip> trips) {
		double sumF = 0.0;
		double sumB = 0.0;
		for (ScheduledTrip trip : trips) {
			Counter counterN = state.getTripCounterN(trip);
			Counter counterB = state.getTripCounterB(trip);
			if (counterN == null || counterB == null)
				throw new IllegalArgumentException("Counters for trip cannot be found.");
			double countB = counterB.getValue();
			double countN = counterN.getValue();
			int seats = trip.composition().getAllSeats();
			double seatsAvailable = Math.max(seats - (countN - countB), 0.0);
			double countF = Math.min(seatsAvailable, countB);
			sumF += countF;
			sumB += countB;
		}
		return sumF/sumB;
	}
}
