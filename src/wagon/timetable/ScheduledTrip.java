package wagon.timetable;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;
import wagon.rollingstock.Composition;

/**
 * This class represents a scheduled departure in a timetable, not taking into 
 * account potential delays. Not to be confused with <code>Departure</code>, 
 * which reflects a departure event in an event-activity network.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class ScheduledTrip implements Comparable<ScheduledTrip> {

	private Composition composition;
	private LocalDateTime arrTime;
	private LocalDateTime depTime;
	private Station toStation;
	private Station fromStation;
	private ComfortNorm norm;
	
	/**
	 * Constructs a <code>ScheduldDeparture</code> object.
	 * 
	 * @param composition	train composition
	 * @param time			scheduled departure time
	 */
	public ScheduledTrip(Composition composition, LocalDateTime depTime, 
			LocalDateTime arrTime, Station fromStation, Station toStation, 
			ComfortNorm norm) {
		this.composition = composition;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.norm = norm;
	}
	
	/**
	 * @return	(simplified) train composition
	 */
	public Composition composition() {
		return composition;
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
	public LocalDateTime departureTime() {
		return depTime;
	}
	
	/**
	 * @return	scheduled arrival time
	 */
	public LocalDateTime arrivalTime() {
		return arrTime;
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
	
	/**
	 * Sets the current arrival time to <code>time</code>.
	 * 
	 * @param time	the new arrival time
	 */
	public void setArrivalTime(LocalDateTime time) {
		arrTime = time;
	}
	
	/**
	 * Sets the current departure time to <code>time</code>.
	 * 
	 * @param time	the new departure time
	 */
	public void setDepartureTime(LocalDateTime time) {
		depTime = time;
	}
	
	/**
	 * Sets the current origin station to <code>station</code>.
	 * 
	 * @param time	the new origin station
	 */
	public void setFromStation(Station station) {
		fromStation = station;
	}
	
	/**
	 * Sets the current destination station to <code>station</code>.
	 * 
	 * @param time	the new destination station
	 */
	public void setToStation(Station station) {
		toStation = station;
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
		return b1 && b3 && b4 && b5;
	}

	@Override
	public int compareTo(ScheduledTrip that) {
		int res1 = departureTime().compareTo(that.departureTime());
		if (res1 != 0)
			return res1;
		int res2 = arrivalTime().compareTo(that.arrivalTime());
		if (res2 != 0)
			return res2;		
		int res3 = fromStation().name().compareTo(that.fromStation().name());
		if (res3 != 0)
			return res3;
		return toStation().name().compareTo(that.toStation().name());
	}
	
	@Override
	public String toString() {
		return "[" + depTime + ": " + fromStation + "\t" + toStation + " " + arrTime + " (" + composition.type() + ")]";
	}
}
