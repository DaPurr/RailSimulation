package wagon.simulation;

import java.time.LocalDateTime;

/**
 * This abstract class is used to model events for a discrete-event simulation.
 * 
 * @author Nemanja Milovanovic
 *
 */
public abstract class Event implements Comparable<Event> {
	
	private SystemState state;
	
	/**
	 * Constructs an <code>Event</code> object.
	 * 
	 * @param	the current state the system state
	 */
	public Event(SystemState state) {
		this.state = state;
	}
	
	/**
	 * Processes this event based on the current system 
	 * state.
	 */
	public abstract void process();
	
	/**
	 * @return	the <code>LocalDateTime</code> representation of the 
	 * 			timing of the event
	 */
	public abstract LocalDateTime time();
	
	@Override
	public int compareTo(Event other) {
		return time().compareTo(other.time());
	}
	
	/**
	 * @return	returns the current system state
	 */
	public SystemState getState() {
		return state;
	}
	
}
