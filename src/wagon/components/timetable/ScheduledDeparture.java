package wagon.components.timetable;

import java.time.LocalTime;

import wagon.components.rollingstock.Composition;

/**
 * This class represents a scheduled departure in a timetable, not taking into 
 * account potential delays. Not to be confused with <code>Departure</code>, 
 * which reflects a departure event in an event-activity network.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class ScheduledDeparture implements Comparable<ScheduledDeparture> {

	private Composition composition;
	private LocalTime time;
	
	/**
	 * Constructs a <code>ScheduldDeparture</code> object.
	 * 
	 * @param composition	train composition
	 * @param time			scheduled departure time
	 */
	public ScheduledDeparture(Composition composition, LocalTime time) {
		this.composition = composition;
		this.time = time;
	}
	
	/**
	 * @return	(simplified) train composition
	 */
	public Composition composition() {
		return composition;
	}
	
	/**
	 * 
	 * @return	scheduled departure time
	 */
	public LocalTime time() {
		return time;
	}

	@Override
	public int compareTo(ScheduledDeparture that) {
		return this.time.compareTo(that.time());
	}
}
