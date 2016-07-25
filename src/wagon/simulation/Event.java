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
		int res1 = time().compareTo(other.time());
		if (res1 != 0)
			return res1;
		// for a trip from A -> B, when we say we board for this trip, we board at A and 
		// when we say we alight this trip we alight at B, so first we process boarding 
		// events, then alighting events.
		return -this.getClass().getName().compareTo(other.getClass().getName());
	}
	
}
