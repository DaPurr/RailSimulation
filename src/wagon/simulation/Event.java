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
	 * Constructs an <code>Event</code> object.
	 * 
	 * @param	the current state the system state
	 */
	public Event() {
		
	}
	
	/**
	 * Processes this event based on the current system 
	 * state.
	 * 
	 * @param	state	the current system state
	 */
	public abstract void process(SystemState state);
	
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
