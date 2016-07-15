package wagon.simulation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import wagon.timetable.ScheduledTrip;

public class Report {

	private SystemState state;
	
	/**
	 * TODO: create new class representing n simulation iterations, 
	 * instead of using SystemState
	 * 
	 * Constructs a <code>Report</code> object.
	 * 
	 * @param state	the system state after simulation
	 */
	public Report(SystemState state) {
		this.state = state;
	}
	
	/**
	 * @return	Returns a short summary of both the current and the new KPI 
	 * 			'transport capacity during rush hour'. The summary consists 
	 * 			of the KPI values overall, and ordered by morning and 
	 * 			afternoon rush hour. 
	 */
	public String summary() {
		String s = "";
		Set<ScheduledTrip> trips = state.getTimetable().getAllTrips();
		s += "ALL TRIPS" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(trips) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(trips) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsMorningRush = getTripsMorningRushHour(trips);
		s += "MORNING RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsMorningRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsMorningRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsAfternoonRush = getTripsAfternoonRushHour(trips);
		s += "AFTERNOON RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAfternoonRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAfternoonRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsAllRush = new HashSet<>(tripsMorningRush);
		tripsAllRush.addAll(tripsAfternoonRush);
		s += "COMBINED RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAllRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAllRush) + System.lineSeparator();
		s += System.lineSeparator();
		
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
			int normCapacity = trip.composition().normCapacity2(trip.getNorm());
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

	/**
	 * 
	 * @param trips	the set of trips
	 * @return	the set of morning rush hour trips
	 */
	public Set<ScheduledTrip> getTripsMorningRushHour(Collection<ScheduledTrip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("07:00"), LocalTime.parse("09:00"));
	}
	
	/**
	 * 
	 * @param trips	the set of trips
	 * @return	the set of afternoon rush hour trips
	 */
	public Set<ScheduledTrip> getTripsAfternoonRushHour(Collection<ScheduledTrip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("16:00"), LocalTime.parse("18:00"));
	}
	
	/**
	 * Returns the set of trips where every trip has an overlap with time window 
	 * [<code>time1</code>, <code>time2</code>], inclusive.
	 * 
	 * @param trips	the set of trips
	 * @param time1	the lower bound of the time horizon
	 * @param time2	the upper bound of the time horizon
	 * @return	the overlapping set of trips
	 */
	public Set<ScheduledTrip> getTripsBetweenTimes(Collection<ScheduledTrip> trips, LocalTime time1, LocalTime time2) {
		Set<ScheduledTrip> setTrips = new HashSet<>();
		for (ScheduledTrip trip : trips) {
			LocalDateTime tripDepartureTime = trip.departureTime();
			LocalDateTime tripArrivalTime = trip.arrivalTime();
			if (tripArrivalTime.toLocalTime().compareTo(time2) <= 0
					&& tripDepartureTime.toLocalTime().compareTo(time1) >= 0) {
				setTrips.add(trip);
			}
		}
		return setTrips;
	}
}
