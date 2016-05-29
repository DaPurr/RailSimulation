package wagon.simulation;

import java.time.LocalDateTime;
import java.util.*;

import wagon.rollingstock.Composition;
import wagon.timetable.ScheduledTrip;

public class AlightingEvent extends Event {
	
	private LocalDateTime time;
	private List<PassengerGroup> groups;
	private ScheduledTrip trip;

	public AlightingEvent(ScheduledTrip trip, List<PassengerGroup> groups, LocalDateTime time) {
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
		
		// get current occupation
		double currentOccupation = state.getOccupation(trainID);
		
		// let passengers alight
		double passengersToBoard = countPassengers(groups);
		state.incrementCounterB(trip, passengersToBoard);
		
		// determine new occupation
		state.setOccupation(trainID, currentOccupation + passengersToBoard);
	}
	
	private double countPassengers(Collection<PassengerGroup> groups) {
		double sum = 0.0;
		for (PassengerGroup group : groups) {
			sum += group.size();
		}
		return sum;
	}

}
