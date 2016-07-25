package wagon.simulation;

import java.time.LocalDateTime;

import wagon.rollingstock.Composition;
import wagon.timetable.ScheduledTrip;

public class AlightingEvent extends Event {
	
	private LocalDateTime time;
	private ScheduledTrip trip;

	public AlightingEvent(ScheduledTrip trip, LocalDateTime time) {
		super();
		this.trip = trip;
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
		
		// get current occupation
		double currentOccupation = state.getOccupation(trainID);
		
		// let passengers alight
		double passengersToAlight = 1;
//		state.setCounterN(trip, currentOccupation - passengersToAlight);
		
		// determine new occupation
		state.setOccupation(trainID, currentOccupation - passengersToAlight);
	}

}
