package wagon.network.expanded;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;
import wagon.network.Node;

/**
 * Represents an event in an event-activity graph.
 * 
 * @author Nemanja Milovanovic
 *
 */

public abstract class EventNode implements Node, Comparable<EventNode> {
	
	private Station station;
	private LocalDateTime time;
	
	public EventNode(Station station, LocalDateTime time) {
		this.station = station;
		this.time = time;
	}
	
	/**
	 * @return	Returns <code>LocalTime</code> object representing the scheduled time 
	 * of occurrence for this particular <code>Event</code>. Cannot discern time on 
	 * a higher level (for example occurrences over different days).
	 */
	public LocalDateTime time() {
		return time;
	}
	
	/**
	 * @return	Returns the (train) station associated with this event.
	 */
	public Station station() {
		return station;
	}
	
	@Override
	public int compareTo(EventNode other) {
		int res1 = station.name().compareTo(other.station.name());
		if (res1 != 0)
			return res1;
		return time().compareTo(other.time());
	}
	
	@Override
	public String toString() {
		return station.name();
	}
}
