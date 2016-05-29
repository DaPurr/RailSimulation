package wagon.simulation;

import java.time.LocalDateTime;
import java.util.List;

import wagon.rollingstock.Composition;
import wagon.timetable.ScheduledTrip;

public class BoardingEvent extends Event {
	
	private LocalDateTime time;
	private List<PassengerGroup> groups;
	private ScheduledTrip trip;

	public BoardingEvent(ScheduledTrip trip, List<PassengerGroup> groups, LocalDateTime time) {
		super();
		this.time = time;
		this.groups = groups;
		this.time = time;
	}

	@Override
	public LocalDateTime time() {
		return time;
	}

	@Override
	public void process(SystemState state) {
		Composition composition = trip.composition();
		int trainID = composition.id();
		double currentOccupation = state.getOccupation(trainID);
	}

}
