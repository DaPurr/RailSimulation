package wagon.simulation;

import java.util.ArrayList;
import java.util.List;

import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

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

	private List<Counter> counters;
	private EventActivityNetwork network;
	private Timetable timetable;
	
	/**
	 * Constructs the system state of a DES.
	 * 
	 * @param network	the network constructed by means of the timetable
	 * @param timetable	the timetable used in this simulation
	 */
	public SystemState(EventActivityNetwork network, Timetable timetable) {
		counters = new ArrayList<>();
		this.network = network;
		this.timetable = timetable;
	}
	
	/**
	 * @return	returns the <code>List</code> of counters
	 */
	public List<Counter> getCounters() {
		return new ArrayList<>(counters);
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
	
	public Counter addCounter(String name, int startValue) {
		Counter counter = new Counter(name, startValue);
		counters.add(counter);
		return counter;
	}
	
	public Counter addCounter(String name) {
		return addCounter(name, 0);
	}
}
