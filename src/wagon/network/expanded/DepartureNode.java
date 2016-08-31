package wagon.network.expanded;

import wagon.timetable.Trip;

public class DepartureNode extends EventNode {

	public DepartureNode(Trip trip) {
		super(trip);
	}
	
	@Override
	public String toString() {
		return "Departure: " + trip().departureTime() + " " + trip().fromStation();
	}
	
}
