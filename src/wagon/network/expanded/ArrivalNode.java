package wagon.network.expanded;

import wagon.timetable.Trip;

public class ArrivalNode extends EventNode {

	public ArrivalNode(Trip trip) {
		super(trip);
	}
	
	@Override
	public String toString() {
		return "Arrival: " + trip().arrivalTime() + " " + trip().toStation();
	}
	
}
