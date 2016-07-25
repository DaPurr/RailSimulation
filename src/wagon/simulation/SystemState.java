package wagon.simulation;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import wagon.data.CiCoData;
import wagon.infrastructure.Station;
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
	
	private final int horizon = 24*60*60;

	// system state variables
	private EventActivityNetwork network;
	private Timetable timetable;
	private Map<Integer, Double> trainOccupation;
	private Map<Journey, ArrivalProcess> arrivalProcesses;
	
	// counters
	private Map<ScheduledTrip, Counter> tripToB; // b_t
	private Map<ScheduledTrip, Counter> tripToN; // n_t
	
	/**
	 * Constructs the system state of a DES.
	 * 
	 * @param network	the network constructed by means of the timetable
	 * @param timetable	the timetable used in this simulation
	 */
	public SystemState(
			EventActivityNetwork network, 
			Timetable timetable, 
			CiCoData cicoData) {
		this.network = network;
		this.timetable = timetable;
		trainOccupation = new LinkedHashMap<>();
		
		tripToB = new LinkedHashMap<>();
		tripToN = new LinkedHashMap<>();
		
		arrivalProcesses = estimateArrivalProcesses(cicoData);
	}
	
	/**
	 * @return	returns an <code>EventActivityNetwork</code> object
	 */
	public EventActivityNetwork getNetwork() {
		return network;
	}
	
	/**
	 * @return	returns the <code>Timetable</code> on which this
	 * 			simulation is based
	 */
	public Timetable getTimetable() {
		return timetable;
	}
	
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
	public double incrementCounterN(ScheduledTrip trip, double incr) {
		if (trip == null)
			throw new IllegalArgumentException("Invalid arguments.");
		Counter count = tripToN.get(trip);
		if (count == null) {
			count = new Counter("n_t#" + trip.toString());
			tripToN.put(trip, count);
		}
		double result = count.increment(incr);
		if (result < 0.0)
			throw new IllegalStateException("Number of passengers aboard " + trip + " cannot be " + result);
		return result;
	}
	
	public void setCounterN(ScheduledTrip trip, double val) {
		if (trip == null || val < 0.0)
			throw new IllegalArgumentException("Invalid arguments.");
		Counter count = tripToN.get(trip);
		if (count == null) {
			count = new Counter("n_t#" + trip.toString());
			tripToN.put(trip, count);
		}
		count.setValue(val);
	}
	
	/**
	 * Increments the counter responsible for determining the number of people 
	 * boarding at trip <code>trip</code>.
	 * 
	 * @param trip	the corresponding trip
	 * @param incr	the increment
	 * @return		returns the increment added to the old value
	 */
	public double incrementCounterB(ScheduledTrip trip, double incr) {
		if (trip == null || incr < 0)
			throw new IllegalArgumentException("Invalid arguments.");
		Counter count = tripToB.get(trip);
		if (count == null) {
			count = new Counter("b_t#" + trip.toString());
			tripToB.put(trip, count);
		}
		return count.increment(incr);
	}
	
	public void setCounterB(ScheduledTrip trip, double val) {
		if (trip == null || val < 0.0)
			throw new IllegalArgumentException("Invalid arguments.");
		Counter count = tripToB.get(trip);
		if (count == null) {
			count = new Counter("b_t#" + trip.toString());
			tripToB.put(trip, count);
		}
		count.setValue(val);
	}
	
	/**
	 * @param trip	the trip
	 * @return	returns the counter for n_t corresponding to <code>trip</code>
	 */
	public Counter getTripCounterN(ScheduledTrip trip) {
		Counter counter = tripToN.get(trip);
		if (counter == null) {
			counter = new Counter("n_t#" + trip.toString());
			tripToN.put(trip, counter);
		}
		return counter;
	}
	
	/**
	 * @param trip	the trip
	 * @return	returns the counter for b_t corresponding to <code>trip</code>
	 */
	public Counter getTripCounterB(ScheduledTrip trip) {
		Counter counter = tripToB.get(trip);
		if (counter == null) {
			counter = new Counter("b_t#" + trip.toString());
			tripToB.put(trip, counter);
		}
		return counter;
	}
	
	/**
	 * @return	returns all trips for which a counter is registered (n_t counter)
	 */
	public Set<ScheduledTrip> getRegisteredTrips() {
		return new LinkedHashSet<>(tripToN.keySet());
	}
	
	public Set<Entry<Journey, ArrivalProcess>> arrivalProcessEntries() {
		return arrivalProcesses.entrySet();
	}
	
	private Map<Journey, ArrivalProcess> estimateArrivalProcesses(CiCoData cicoData) {
		// group passengers based on their journeys
		Multimap<Journey, Passenger> map = LinkedHashMultimap.create();
		for (Passenger passenger : cicoData.getPassengers()) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			Journey journey = new Journey(from, to);
			map.put(journey, passenger);
		}
		
		// for each journey, estimate arrival process
		Map<Journey, ArrivalProcess> resultMap = new HashMap<>();
		int seed = 1;
		double maxLambda = Double.NEGATIVE_INFINITY;
		for (Journey journey : map.keySet()) {
			Collection<Passenger> passengers = map.get(journey);
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, horizon, 5*60, seed);
//			ArrivalProcess arrivalProcess = new PiecewiseConstantProcess(passengers, 5*60, seed);
			resultMap.put(journey, arrivalProcess);
			double lambda = arrivalProcess.getLambdaUpperBound();
			if (lambda > maxLambda)
				maxLambda = lambda;
			seed++;
		}
		return resultMap;
	}
}
