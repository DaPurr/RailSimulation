package wagon.components.timetable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wagon.components.infrastructure.Station;

/**
 * This class represents a train timetable, where we store the departures per station. 
 * It is designed mainly to construct timetable-loyal networks.
 * 
 * @author Nemanja Milovanovic
 *
 */

public class Timetable {
	
	private Map<Station, List<ScheduledDeparture>> departures;
	
	public Timetable() {
		departures = new HashMap<>();
	}
	
	/**
	 * This method maps a station to a list of departures, where the departures are 
	 * meant to be used as reference. Note: the departures are sorted in ascending 
	 * order according to departure time.
	 * 
	 * @param station	origin station
	 * @param dep		<code>ScheduledDeparture</code> object giving time of 
	 * 					departure, and destination station.
	 */
	public void addStation(Station station, List<ScheduledDeparture> dep) {
		List<ScheduledDeparture> deps = new ArrayList<>(dep);
		Collections.sort(deps);
		departures.put(station, deps);
	}
	
	/**
	 * Adds a single departure to the timetable, where the departure is mapped 
	 * to a station. The departures are then sorted. 
	 * 
	 * @param station		station of departure
	 * @param departure		<code>ScheduledDeparture</code> object representing 
	 * 						the actual departure
	 */
	public void addStation(Station station, ScheduledDeparture departure) {
		if (station == null || departure == null)
			throw new IllegalArgumentException("Arguments can't be null");
		if (!departures.containsKey(station)) {
			List<ScheduledDeparture> deps = new ArrayList<>();
			deps.add(departure);
			departures.put(station, deps);
		} else {
			List<ScheduledDeparture> deps = departures.get(station);
			deps.add(departure);
			Collections.sort(deps);
		}
	}
	
	/**
	 * @param 	station	departure station
	 * @return	sorted list of departure from <code>station</code>
	 */
	public List<ScheduledDeparture> departuresByStation(Station station) {
		if (station == null || !departures.containsKey(station))
			throw new IllegalArgumentException("Station not available: " + station);
		return new ArrayList<>(departures.get(station));
	}
	
	public int size() {
		return departures.size();
	}
}
