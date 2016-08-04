package wagon.simulation;

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
		TrainService composition = trip.getTrainService();
		int trainID = composition.id();
		
		// get current occupation
		double currentOccupation = state.getOccupation(trainID);
		
		// let passengers alight
		double passengersToAlight = 1;
//		state.setCounterN(trip, currentOccupation - passengersToAlight);
		
		// determine new occupation
		double oldOccupation = state.setOccupation(trainID, currentOccupation - passengersToAlight);
		if (oldOccupation < currentOccupation-passengersToAlight)
			throw new IllegalStateException("New occupation cannot be higher when alighting");
	}

}
