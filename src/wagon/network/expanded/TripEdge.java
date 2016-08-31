package wagon.network.expanded;

import wagon.network.WeightedEdge;
import wagon.timetable.Trip;

/**
 * Wrapper class for trip edges in the event-activity network.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class TripEdge extends WeightedEdge {
	
	private Trip trip;

	/**
	 * @param trip		trip associated to this trip edge
	 * @param weight	waiting time until next event
	 */
	public TripEdge(EventNode source, EventNode target, 
			Trip trip, double weight) {
		super(source, target, weight);
		this.trip = trip;
	}
	
	/**
	 * @return	the wrapped trip
	 */
	public Trip trip() {
		return trip;
	}
	
	@Override
	public String toString() {
		return "TRIP: " + trip.toString();
	}

}
