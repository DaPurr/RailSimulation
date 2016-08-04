package wagon.timetable;

import java.time.LocalTime;

import wagon.infrastructure.Station;
import wagon.rollingstock.TrainService;

/**
 * This class represents a scheduled departure in a timetable, not taking into 
 * account potential delays. Not to be confused with <code>Departure</code>, 
 * which reflects a departure event in an event-activity network.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class ScheduledTrip implements Comparable<ScheduledTrip> {

	private TrainService trainService;
	private LocalTime arrTime;
	private LocalTime depTime;
	private Station toStation;
	private Station fromStation;
	private ComfortNorm norm;
	private int dayOfWeek;
	
	/**
	 * Constructs a <code>ScheduldDeparture</code> object.
	 * 
	 * @param composition	train composition
	 * @param time			scheduled departure time
	 */
	public ScheduledTrip(TrainService trainService, LocalTime depTime, 
			LocalTime arrTime, Station fromStation, Station toStation, 
			ComfortNorm norm, int dayOfWeek) {
		this.trainService = trainService;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.norm = norm;
		this.dayOfWeek = dayOfWeek;
	}
	
	/**
	 * @return	(simplified) train composition
	 */
	public TrainService getTrainService() {
		return trainService;
	}
	
	public void setTrainService(TrainService service) {
		this.trainService = service;
	}
	
	/**
	 * @return	returns the norm for this trip, which is a member of 
	 * 			<code>ComfortNorm</code>.
	 */
	public ComfortNorm getNorm() {
		return norm;
	}
	
	/**
	 * @return	scheduled departure time
	 */
	public LocalTime departureTime() {
		return depTime;
	}
	
	public void setDepartureTime(LocalTime time) {
		depTime = time;
	}
	
	/**
	 * @return	scheduled arrival time
	 */
	public LocalTime arrivalTime() {
		return arrTime;
	}
	
	public void setArrivalTime(LocalTime time) {
		arrTime = time;
	}
	
	/**
	 * @return	arrival station
	 */
	public Station toStation() {
		return toStation;
	}
	
	/**
	 * @return	departure station
	 */
	public Station fromStation() {
		return fromStation;
	}
	
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	
	public ScheduledTrip copy() {
		ScheduledTrip trip = new ScheduledTrip(
				trainService.copy(), 
				depTime, 
				arrTime, 
				fromStation, 
				toStation, 
				norm, 
				dayOfWeek);
		return trip;
	}
	
	@Override
	public int hashCode() {
		int hc1 = arrTime.hashCode();
		int hc2 = depTime.hashCode();
		int hc3 = toStation.hashCode();
		int hc4 = fromStation.hashCode();
		int hc5 = Integer.hashCode(dayOfWeek);
		int hc6 = Integer.hashCode(trainService.id());
		return 3*hc1 + 5*hc2 + 7*hc3 + 11*hc4 + 13*hc5 + 17*hc6;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ScheduledTrip))
			return false;
		ScheduledTrip o = (ScheduledTrip) other;
		boolean b1 = this.arrTime.equals(o.arrTime);
		boolean b3 = this.depTime.equals(o.depTime);
		boolean b4 = this.fromStation.equals(o.fromStation);
		boolean b5 = this.toStation.equals(o.toStation);
		boolean b6 = this.dayOfWeek == o.dayOfWeek;
		boolean b2 = this.trainService.id() == o.trainService.id();
		return b2 && b6 && b1 && b3 && b4 && b5;
	}

	@Override
	public int compareTo(ScheduledTrip that) {
		int res0 = Integer.compare(trainService.id(), that.trainService.id());
		if (res0 != 0)
			return res0;
		int res1 = Integer.compare(this.dayOfWeek, that.dayOfWeek);
		if (res1 != 0)
			return res1;
		int res2 = departureTime().compareTo(that.departureTime());
		if (res2 != 0)
			return res2;
		int res3 = arrivalTime().compareTo(that.arrivalTime());
		if (res3 != 0)
			return res3;		
		int res4 = fromStation().name().compareTo(that.fromStation().name());
		if (res4 != 0)
			return res4;
		return toStation().name().compareTo(that.toStation().name());
	}
	
	@Override
	public String toString() {
		return "[day: " + dayOfWeek + ", " + depTime + ": " + fromStation + "\t" + toStation + " " + arrTime + " (" + trainService.toString() + ")]";
	}
}
