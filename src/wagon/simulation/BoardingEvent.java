package wagon.simulation;

import java.time.LocalDateTime;

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
	private ScheduledTrip trip;

	/**
	 * Constructs a <code>BoardingEvent</code> object.
	 * 
	 * @param trip		the trip associated to the boarding
	 * @param groups	the passenger groups about to board the train
	 * @param time		the time at which the event takes place
	 */
	public BoardingEvent(ScheduledTrip trip, LocalDateTime time) {
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
		
		// let passengers board
		double passengersToBoard = 1;
		state.incrementCounterB(trip, passengersToBoard);
		state.setCounterN(trip, currentOccupation + passengersToBoard);
		
		// determine new occupation
		state.setOccupation(trainID, currentOccupation + passengersToBoard);
	}

}
