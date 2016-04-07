package wagon.components.network.expanded;

import java.time.LocalTime;

import wagon.components.infrastructure.Station;
import wagon.components.network.Node;

/**
 * Represents an event in an event-activity graph.
 * 
 * @author Nemanja Milovanovic
 *
 */

public interface Event extends Node {
	
	/**
	 * @return	Returns <code>LocalTime</code> object representing the scheduled time 
	 * of occurrence for this particular <code>Event</code>. Cannot discern time on 
	 * a higher level (for example occurrences over different days).
	 */
	public LocalTime getTime();
	
	@Override
	public String toString();
	
	/**
	 * @return	Returns the (train) station associated with this event.
	 */
	public Station getStation();
}
