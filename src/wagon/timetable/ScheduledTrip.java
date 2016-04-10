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
	
	/**
	 * Constructs a <code>ScheduldDeparture</code> object.
	 * 
	 * @param composition	train composition
	 * @param time			scheduled departure time
	 */
	public ScheduledTrip(Composition composition, LocalDateTime depTime, 
			LocalDateTime arrTime, Station fromStation, Station toStation) {
		this.composition = composition;
		this.depTime = depTime;
		this.arrTime = arrTime;
		this.fromStation = fromStation;
		this.toStation = toStation;
	}
	
	/**
	 * @return	(simplified) train composition
	 */
	public Composition composition() {
		return composition;
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

	@Override
	public int compareTo(ScheduledTrip that) {
		int res1 = fromStation().name().compareTo(that.fromStation().name());
		if (res1 != 0)
			return res1;
		int res2 = toStation().name().compareTo(that.toStation().name());
		if (res2 != 0)
			return res2;
		int res3 = departureTime().compareTo(that.departureTime());
		if (res3 != 0)
			return res3;
		return arrivalTime().compareTo(that.arrivalTime());
	}
	
	@Override
	public String toString() {
		return "[" + depTime + ": " + fromStation + "\t" + toStation + arrTime + "(" + composition.type() + ")]";
	}
}
