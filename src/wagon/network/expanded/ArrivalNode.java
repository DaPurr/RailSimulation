package wagon.network.expanded;

import wagon.timetable.ScheduledTrip;

public class ArrivalNode extends EventNode {

	public ArrivalNode(ScheduledTrip trip) {
		super(trip);
	}
	
	@Override
	public String toString() {
		return "Arrival: " + trip().arrivalTime() + " " + trip().toStation();
	}
	
}
