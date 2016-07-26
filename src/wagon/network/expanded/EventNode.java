package wagon.network.expanded;

import java.time.LocalDateTime;
import java.time.LocalTime;

import wagon.network.Node;
import wagon.timetable.ScheduledTrip;

/**
 * Represents an event in an event-activity graph.
 * 
 * @author Nemanja Milovanovic
 *
 */

public abstract class EventNode implements Node, Comparable<EventNode> {
	
	private ScheduledTrip trip;
	
	public EventNode(ScheduledTrip trip) {
		this.trip = trip;
	}
	
	/**
	 * @return	Returns the associated trip
	 */
	public ScheduledTrip trip() {
		return trip;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EventNode))
			return false;
		EventNode other = (EventNode) o;
		boolean b1 = this.getClass().equals(other.getClass());
		boolean b2 = this.trip.equals(other.trip);
		boolean b3 = this.trip.composition().equals(other.trip.composition());
		return b1 && b2 && b3;
	}
	
	@Override
	public int compareTo(EventNode other) {
		LocalTime time1 = null;
		LocalTime time2 = null;
		String name1 = null;
		String name2 = null;
		String name3 = null;
		String name4 = null;
		if (this instanceof DepartureNode) {
			time1 = this.trip.departureTime();
			name1 = this.trip.fromStation().name();
			name3 = this.trip.toStation().name();
		} else {
			time1 = this.trip.arrivalTime();
			name1 = this.trip.toStation().name();
			name3 = this.trip.fromStation().name();
		}
		if (other instanceof ArrivalNode) {
			time2 = other.trip.arrivalTime();
			name2 = other.trip.toStation().name();
			name4 = other.trip.fromStation().name();
		} else {
			time2 = other.trip.departureTime();
			name2 = other.trip.fromStation().name();
			name4 = other.trip.toStation().name();
		}
		
		int res1 = name1.compareTo(name2);
		int res2 = time1.compareTo(time2);
		int res3 = name3.compareTo(name4);
		int res4 = this.getClass().getName().compareTo(other.getClass().getName());
		if (res1 != 0)
			return res1;
		if (res2 != 0)
			return res2;
		if (res4 != 0)
			return res4;
		if (res3 != 0)
			return res3;
		return this.trip.composition().id() - other.trip.composition().id();
	}
}
