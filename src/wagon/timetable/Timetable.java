package wagon.timetable;

import java.util.*;

import wagon.infrastructure.Station;
import wagon.rollingstock.Composition;

/**
 * This class represents a train timetable, where we store the departures per station. 
 * It is designed mainly to construct timetable-loyal networks.
 * 
 * @author Nemanja Milovanovic
 * 
 */

public class Timetable {
	
	private Map<Station, List<ScheduledTrip>> trips;
	private Map<Composition,SortedSet<ScheduledTrip>> routes;
	
	public Timetable() {
		trips = new HashMap<>();
		routes = new HashMap<>();
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
	public void addStation(Station station, List<ScheduledTrip> dep) {
		List<ScheduledTrip> deps = new ArrayList<>(dep);
		Collections.sort(deps);
		trips.put(station, deps);
	}
	
	/**
	 * Adds a single departure to the timetable, where the departure is mapped 
	 * to a station. The departures are then sorted. 
	 * 
	 * @param station		station of departure
	 * @param departure		<code>ScheduledDeparture</code> object representing 
	 * 						the actual departure
	 */
	public void addStation(Station station, ScheduledTrip trip) {
		if (station == null || trip == null)
			throw new IllegalArgumentException("Arguments can't be null");
		if (!trips.containsKey(station)) {
			List<ScheduledTrip> deps = new ArrayList<>();
			deps.add(trip);
			trips.put(station, deps);
		} else {
			List<ScheduledTrip> deps = trips.get(station);
			deps.add(trip);
			Collections.sort(deps);
		}
		
		// add trip to composition route
		addTrip(trip);
	}
	
	public List<ScheduledTrip> getRoute(Composition comp) {
		if (!routes.containsKey(comp))
			return null;
		return new ArrayList<>(routes.get(comp));
	}
	
	/**
	 * @param 	station	departure station
	 * @return	sorted list of departures from <code>station</code>
	 */
	public List<ScheduledTrip> departuresByStation(Station station) {
		if (station == null || !trips.containsKey(station))
			throw new IllegalArgumentException("Station not available: " + station);
		return new ArrayList<>(trips.get(station));
	}
	
	/**
	 * @return	number of stations
	 */
	public int size() {
		return trips.size();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Timetable))
			return false;
		Timetable o = (Timetable) other;
		return this.trips.equals(o.trips) && this.routes.equals(o.routes);
	}
	
	@Override
	public int hashCode() {
		return 5*trips.hashCode() + 7*routes.hashCode();
	}
	
	@Override
	public String toString() {
		String s = "[\n";
		for (Station station : trips.keySet()) {
			s += "  ";
			s += station.name();
			if (trips.get(station) == null)
				continue;
			List<ScheduledTrip> deps = departuresByStation(station);
			for (int i = 0; i < deps.size(); i++) {
				s += "\t";
				if (i > 0)
					s += "\t";
				ScheduledTrip dep = deps.get(i);
				s += dep.toStation().name() + " ";
				s += dep.departureTime() + " ";
				s += dep.composition().type().toString() + "_"
						+ dep.composition().getNrWagons() + "\n";
			}
		}
		s += "]";
		return s;
	}
	
	/**
	 * @return	set of train stations
	 */
	public Set<Station> stations() {
		return new HashSet<>(trips.keySet());
	}
	
	private void addTrip(ScheduledTrip trip) {
		Composition comp = trip.composition();
		if (!routes.containsKey(comp)) {
			SortedSet<ScheduledTrip> set = new TreeSet<>();
			set.add(trip);
			routes.put(comp, set);
		} else {
			SortedSet<ScheduledTrip> set = routes.get(comp);
			set.add(trip);
		}
	}
}
