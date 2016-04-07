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
	 * Returns <code>LocalTime</code> object representing the scheduled time of occurrence for this particular <code>Event</code>.
	 * Cannot discern time on a higher level (for example occurrences over different days).
	 * 
	 * @return	scheduled time of occurrence as <code>LocalTime</code>
	 */
	public LocalTime getTime();
	
	@Override
	public String toString();
	
	/**
	 * Returns the (train) station associated with this event. 
	 * 
	 * @return	the corresponding <code>Station</code> object
	 */
	public Station getStation();
}
