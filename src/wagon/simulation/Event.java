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
		int comp_time = time().compareTo(other.time());
		if (comp_time != 0)
			return comp_time;
		return this.getClass().getName().compareTo(other.getClass().getName());
	}
	
}
