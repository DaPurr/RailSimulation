package wagon.simulation;

import java.time.LocalDateTime;
import java.util.*;

import wagon.rollingstock.Composition;
import wagon.timetable.ScheduledTrip;

/**
 * This class represents a boarding event. The <code>process</code> method is overridden to 
 * update the counters affected by boarding passengers. In short, b_t and n_t are updated.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class BoardingEvent extends Event {
	
	private LocalDateTime time;
	private List<PassengerGroup> groups;
	private ScheduledTrip trip;

	/**
	 * Constructs a <code>BoardingEvent</code> object.
	 * 
	 * @param trip		the trip associated to the boarding
	 * @param groups	the passenger groups about to board the train
	 * @param time		the time at which the event takes place
	 */
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
		
		// get current occupation
		double currentOccupation = state.getOccupation(trainID);
		
		// let passengers board
		double passengersToAlight = countPassengers(groups);
		state.incrementCounterB(trip, -passengersToAlight);
		
		// determine new occupation
		state.setOccupation(trainID, currentOccupation - passengersToAlight);
	}
	
	private double countPassengers(Collection<PassengerGroup> groups) {
		double sum = 0.0;
		for (PassengerGroup group : groups) {
			sum += group.size();
		}
		return sum;
	}

}
