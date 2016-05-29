package wagon.simulation;

import java.util.*;

import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class SimModel {

	private SystemState state;
	private PriorityQueue<Event> eventQueue;
	
	public SimModel(Timetable timetable, EventActivityNetwork network) {
		state = new SystemState(network, timetable);
		eventQueue = new PriorityQueue<>();
	}
	
	public void start() {
		initialize();
		
		while (!eventQueue.isEmpty()) {
			Event event = eventQueue.poll();
			event.process(state);
		}
	}
	
	private void initialize() {
		
	}
	
	
}
