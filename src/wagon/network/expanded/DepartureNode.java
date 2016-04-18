package wagon.network.expanded;

import wagon.timetable.ScheduledTrip;

public class DepartureNode extends EventNode {

	public DepartureNode(ScheduledTrip trip) {
		super(trip);
	}
	
	@Override
	public String toString() {
		return "Departure: " + trip().departureTime() + " " + trip().fromStation();
	}
	
}
