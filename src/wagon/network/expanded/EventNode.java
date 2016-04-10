package wagon.network.expanded;

import java.time.LocalTime;

import wagon.infrastructure.Station;
import wagon.network.Node;

/**
 * Represents an event in an event-activity graph.
 * 
 * @author Nemanja Milovanovic
 *
 */

public abstract class EventNode implements Node {
	
	private Station station;
	private LocalTime time;
	
	public EventNode(Station station, LocalTime time) {
		this.station = station;
		this.time = time;
	}
	
	/**
	 * @return	Returns <code>LocalTime</code> object representing the scheduled time 
	 * of occurrence for this particular <code>Event</code>. Cannot discern time on 
	 * a higher level (for example occurrences over different days).
	 */
	public LocalTime time() {
		return time;
	}
	
	/**
	 * @return	Returns the (train) station associated with this event.
	 */
	public Station station() {
		return station;
	}
}
