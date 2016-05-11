package wagon.simulation;

import java.time.LocalDateTime;

/**
 * This abstract class is used to model events for a discrete-event simulation.
 * 
 * @author Nemanja Milovanovic
 *
 */
public abstract class Event implements Comparable<Event> {
	
	/**
	 * @return	the <code>LocalDateTime</code> representation of the 
	 * 			timing of the event
	 */
	public abstract LocalDateTime time();
	
	@Override
	public int compareTo(Event other) {
		return time().compareTo(other.time());
	}
	
}
