package wagon.network.expanded;

import wagon.network.WeightedEdge;
import wagon.timetable.ScheduledTrip;

/**
 * Wrapper class for trip edges in the event-activity network.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class TripEdge extends WeightedEdge {
	
	private ScheduledTrip trip;

	/**
	 * @param trip		trip associated to this trip edge
	 * @param weight	waiting time until next event
	 */
	public TripEdge(EventNode source, EventNode target, 
			ScheduledTrip trip, double weight) {
		super(source, target, weight);
		this.trip = trip;
	}
	
	/**
	 * @return	the wrapped trip
	 */
	public ScheduledTrip trip() {
		return trip;
	}
	
	@Override
	public String toString() {
		return "TRIP: " + trip.toString();
	}

}
