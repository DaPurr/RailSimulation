package wagon.simulation;

import java.util.*;

import wagon.data.CiCoData;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.*;

/**
 * This class represents the DES system state. It contains a list of all counters, and the 
 * timetable with corresponding event-activity network used in the simulation.
 * 
 * Moreover, it provides useful methods to access information contained in the media it stores, 
 * such as the set of trips performed by a certain train. 
 * 
 * @author Nemanja Milovanovic
 *
 */
public class SystemState {
	
//	private final int horizon = 24*60*60;

	// system state variables
	private EventActivityNetwork network;
	private Timetable plannedTimetable;
	private Map<Integer, Double> trainOccupation;
	
	// counters
	Map<Trip, TripCounters> tripToCounters;
	
	/**
	 * Constructs the system state of a DES.
	 * 
	 * @param network	the network constructed by means of the timetable
	 * @param timetable	the timetable used in this simulation
	 */
	public SystemState(
			EventActivityNetwork network, 
			Timetable plannedTimetable, 
//			Timetable realizedTimetable, 
			CiCoData cicoData) {
		this.network = network;
		this.plannedTimetable = plannedTimetable;
//		this.realizedTimetable = realizedTimetable;
		trainOccupation = new LinkedHashMap<>();
		
		tripToCounters = new LinkedHashMap<>();
		
		
//		arrivalProcesses = estimateArrivalProcesses(cicoData);
	}
	
	/**
	 * @return	returns an <code>EventActivityNetwork</code> object
	 */
	public EventActivityNetwork getNetwork() {
		return network;
	}
	
	/**
	 * @return	returns the planned <code>Timetable</code> on which this
	 * 			simulation is based
	 */
	public Timetable getTimetable() {
		return plannedTimetable;
	}
	
//	/**
//	 * @return	returns the realized <code>Timetable</code> for this simulation
//	 */
//	public Timetable getRealizedTimetable() {
//		return realizedTimetable;
//	}
	
	/**
	 * Returns the occupation of the the train identified by <code>trainNumber</code>. 
	 * If there is no train with number <code>trainNumber</code>, then 0.0 is 
	 * returned. 
	 * 
	 * @param trainNumber	the train number
	 * @return				(fractional) number of people inside the train
	 */
	public double getOccupation(int trainNumber) {
		if (trainNumber < 0)
			throw new IllegalArgumentException("Train number must be non-negative.");
		Double occupation = trainOccupation.get(trainNumber);
		if (occupation == null)
			return 0.0;
		return occupation;
	}
	
	/**
	 * Updates the train occupation of train <code>trainNumber</code> to 
	 * <code>occupation</code>, then returns the previous value. 
	 * 
	 * @param trainNumber	the train number
	 * @param occupation	the new amount of people occupying train <code>trainNumber</code>
	 * @return				the old amount of people occupying train <code>trainNumber</code>
	 */
	public double setOccupation(int trainNumber, double occupation) {
		if (trainNumber < 0 || occupation < 0.0)
			throw new IllegalArgumentException("Arguments must be non-negative.");
		Double oldValue = trainOccupation.get(trainNumber);
		trainOccupation.put(trainNumber, occupation);
		if (oldValue == null)
			return 0.0;
		return oldValue;
	}
	
	/**
	 * Increments the counter responsible for determining the number of people 
	 * just before trip departure.
	 * 
	 * @param trip	the corresponding trip
	 * @param incr	the increment
	 * @return		returns the increment added to the old value
	 */
	public double incrementCounterN(Trip trip, double incr) {
		if (trip == null)
			throw new IllegalArgumentException("Invalid arguments.");
		TripCounters counters = tripToCounters.get(trip);
		if (counters == null) {
			ComfortNorm norm = trip.getNorm();
			counters = new TripCounters(
					0.0, 
					0.0, 
					trip.getTrainService().normCapacity1(norm) + trip.getTrainService().normCapacity2(norm), 
					trip.getTrainService().getAllSeats());
			tripToCounters.put(trip, counters);
		}
		double n = counters.getN();
		counters.setN(n+1);
		if (n+1 < 0.0)
			throw new IllegalStateException("Number of passengers aboard " + trip + " cannot be " + (n+1));
		return (n+1);
	}
	
	public void setCounterN(Trip trip, double val) {
		if (trip == null || val < 0.0)
			throw new IllegalArgumentException("Invalid arguments.");
		TripCounters counters = tripToCounters.get(trip);
		if (counters == null) {
			ComfortNorm norm = trip.getNorm();
			counters = new TripCounters(
					0.0, 
					0.0, 
					trip.getTrainService().normCapacity1(norm) + trip.getTrainService().normCapacity2(norm), 
					trip.getTrainService().getAllSeats());
			tripToCounters.put(trip, counters);
		}
		counters.setN(val);
	}
	
	/**
	 * Increments the counter responsible for determining the number of people 
	 * boarding at trip <code>trip</code>.
	 * 
	 * @param trip	the corresponding trip
	 * @param incr	the increment
	 * @return		returns the increment added to the old value
	 */
	public double incrementCounterB(Trip trip, double incr) {
		if (trip == null)
			throw new IllegalArgumentException("Invalid arguments.");
		TripCounters counters = tripToCounters.get(trip);
		if (counters == null) {
			ComfortNorm norm = trip.getNorm();
			counters = new TripCounters(
					0.0, 
					0.0, 
					trip.getTrainService().normCapacity1(norm) + trip.getTrainService().normCapacity2(norm), 
					trip.getTrainService().getAllSeats());
			tripToCounters.put(trip, counters);
		}
		double n = counters.getB();
		counters.setB(n+1);
		if (n+1 < 0.0)
			throw new IllegalStateException("Number of passengers aboard " + trip + " cannot be " + (n+1));
		return (n+1);
	}
	
	public void setCounterB(Trip trip, double val) {
		if (trip == null || val < 0.0)
			throw new IllegalArgumentException("Invalid arguments.");
		TripCounters counters = tripToCounters.get(trip);
		if (counters == null) {
			ComfortNorm norm = trip.getNorm();
			counters = new TripCounters(
					0.0, 
					0.0, 
					trip.getTrainService().normCapacity1(norm) + trip.getTrainService().normCapacity2(norm), 
					trip.getTrainService().getAllSeats());
			tripToCounters.put(trip, counters);
		}
		counters.setB(val);
	}
	
	/**
	 * @return	returns all trips for which a counter is registered (n_t counter)
	 */
	public Set<Trip> getRegisteredTrips() {
		return new LinkedHashSet<>(tripToCounters.keySet());
	}
}
