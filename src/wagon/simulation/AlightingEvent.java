package wagon.simulation;

import java.time.LocalDateTime;
import java.time.LocalTime;

import wagon.rollingstock.TrainService;
import wagon.timetable.ScheduledTrip;

public class AlightingEvent extends Event {
	
	private LocalTime time;
	private ScheduledTrip trip;

	public AlightingEvent(ScheduledTrip trip, LocalTime time) {
		super();
		this.trip = trip;
		this.time = time;
	}

	@Override
	public LocalTime time() {
		return time;
	}

	@Override
	public void process(SystemState state) {
		TrainService composition = trip.composition();
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
