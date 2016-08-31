package wagon.simulation;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;

import wagon.timetable.Trip;

public class Report {

//	private SystemState state;
	Map<Trip, TripCounters> tripToCounters;
	
	private final double cicoCorrectionFactor = 1.215767122505321;
	
	private Map<Journey, Set<Trip>> journeyTrips;
	
	/**
	 * Constructs a <code>Report</code> object.
	 * 
	 * @param state	the system state after simulation
	 */
	public Report(Map<Trip, TripCounters> tripToCounters, int dayOfWeek, Map<Journey, Set<Trip>> journeyTrips) {
		this.tripToCounters = tripToCounters;
		this.journeyTrips = journeyTrips;
	}
	
	/**
	 * @return	Returns a short summary of both the current and the new KPI 
	 * 			'transport capacity during rush hour'. The summary consists 
	 * 			of the KPI values overall, and ordered by morning and 
	 * 			afternoon rush hour. 
	 */
	public String summary() {
		String s = "";
		s += "ALL TRIPS" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripToCounters.keySet()) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripToCounters.keySet()) + System.lineSeparator();
		s += System.lineSeparator();
		Set<Trip> tripsMorningRush = getTripsMorningRushHour(tripToCounters.keySet());
		s += "MORNING RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsMorningRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsMorningRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<Trip> tripsAfternoonRush = getTripsAfternoonRushHour(tripToCounters.keySet());
		s += "AFTERNOON RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAfternoonRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAfternoonRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<Trip> tripsAllRush = new HashSet<>(tripsMorningRush);
		tripsAllRush.addAll(tripsAfternoonRush);
		s += "COMBINED RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAllRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAllRush) + System.lineSeparator();
		s += System.lineSeparator();
		
		return s;
	}
	
	public Set<Trip> getAllTrips() {
		return new LinkedHashSet<>(tripToCounters.keySet());
	}
	
	public Set<Trip> getTripsOfService(int id) {
		Set<Trip> set = new HashSet<>();
		for (Trip trip : tripToCounters.keySet()) {
			if (trip.getTrainService().id() == id)
				set.add(trip);
		}
		return set;
	}
	
	public void exportToFile(String file_name) throws IOException {
		// TODO: Ability to export results to file
	}
	
	/**
	 * Given a collection of trips, calculate the old KPI.
	 * 
	 * @param trips	the collection of trips
	 * @return	the old KPI for transport capacity
	 */
	public double calculateKPIOld(Collection<Trip> trips) {
		double numerator = 0.0;
		double denominator = 0.0;
		for (Trip trip : trips) {
			TripCounters tripCounters = tripToCounters.get(trip);
			if (tripCounters == null)
				throw new IllegalArgumentException("Counters for trip cannot be found.");
			double countN = tripCounters.getN();
			countN *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
			double normCapacity = tripCounters.getNormCapacity();
			numerator += countN*Math.min(normCapacity/countN, 1);
			denominator += countN;
		}
		return numerator/denominator;
	}
	
	/**
	 * Given a collection of trips, calculate the new KPI.
	 * 
	 * @param trips	the collection of trips
	 * @return	the new KPI for transport capacity
	 */
	public double calculateKPINew(Collection<Trip> trips) {
		double sumF = 0.0;
		double sumB = 0.0;
		for (Trip trip : trips) {
			TripCounters tripCounters = tripToCounters.get(trip);
			if (tripCounters == null)
				throw new IllegalArgumentException("Counters for trip cannot be found.");
			double countB = tripCounters.getB();
			countB *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
			double countN = tripCounters.getN();
			countN *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
			double seats = tripCounters.getSeatCapacity();
			double seatsAvailable = Math.max(seats - (countN - countB), 0.0);
			double countF = Math.min(seatsAvailable, countB);
			sumF += countF;
			sumB += countB;
		}
		return sumF/sumB;
	}

	/**
	 * 
	 * @param trips	the set of trips
	 * @return	the set of morning rush hour trips
	 */
	public Set<Trip> getTripsMorningRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("07:00"), LocalTime.parse("09:00"));
	}
	
	/**
	 * 
	 * @param trips	the set of trips
	 * @return	the set of afternoon rush hour trips
	 */
	public Set<Trip> getTripsAfternoonRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("16:00"), LocalTime.parse("18:00"));
	}
	
	public Set<Trip> getTripsFromTrain(int trainNumber, Collection<Trip> trips) {
		Set<Trip> set = new HashSet<>();
		for (Trip trip : trips) {
			if (trip.getTrainService().id() == trainNumber)
				set.add(trip);
		}
		return set;
	}
	
	public String reportWorstJourneys() {
		List<JourneyWithKPI> journeyList = new ArrayList<>();
		for (Entry<Journey, Set<Trip>> entry : journeyTrips.entrySet()) {
 			Journey journey = entry.getKey();
			double kpiNew = calculateKPINew(entry.getValue());
			double kpiOld = calculateKPIOld(entry.getValue());
			journeyList.add(new JourneyWithKPI(journey, kpiNew, kpiOld));
		}
		
		Collections.sort(journeyList);
		
		// report worst 15
		String s = "";
		s += System.lineSeparator();
		s += "WORST 10 JOURNEYS" + System.lineSeparator();
		s += "===================" + System.lineSeparator();
		for (int i = 0; i < 15; i++) {
			if (i >= journeyList.size())
				break;
			JourneyWithKPI journeyKPI = journeyList.get(i);
			s += journeyKPI.journey + ": KPI_{new}=" + journeyKPI.kpiNew + "\tKPI_{old}=" + journeyKPI.kpiOld + System.lineSeparator();
		}
		return s;
	}
	
	public String reportWorstTrains() {
		Map<Integer, Collection<Trip>> trainMap = new HashMap<>();
//		Collection<ScheduledTrip> trips = state.getTimetable().getAllTrips(dayOfWeek);
		
		// add trips to all train numbers
		for (Trip trip : tripToCounters.keySet()) {
			int trainNr = trip.getTrainService().id();
			Collection<Trip> collection = trainMap.get(trainNr);
			if (collection == null) {
				collection = new ArrayList<>();
				trainMap.put(trainNr, collection);
			}
			collection.add(trip);
		}
		
		List<TrainWithKPI> trainList = new ArrayList<>();
		for (Entry<Integer, Collection<Trip>> entry : trainMap.entrySet()) {
			int trainNr = entry.getKey();
			double kpiNew = calculateKPINew(entry.getValue());
			double kpiOld = calculateKPIOld(entry.getValue());
			trainList.add(new TrainWithKPI(trainNr, kpiNew, kpiOld));
		}
		
		Collections.sort(trainList);
		
		// report worst 10
		String s = "";
		s += System.lineSeparator();
		s += "WORST 10 TRAINS" + System.lineSeparator();
		s += "===============" + System.lineSeparator();
		for (int i = 0; i < 10; i++) {
			if (i >= trainList.size())
				break;
			TrainWithKPI trainKPI = trainList.get(i);
			s += trainKPI.trainNr + ": KPI_{new}=" + trainKPI.kpiNew + "\tKPI_{old}=" + trainKPI.kpiOld + System.lineSeparator();
		}
		return s;
	}
	
	/**
	 * Returns the set of trips where every trip has an overlap with time window 
	 * [<code>time1</code>, <code>time2</code>], inclusive.
	 * 
	 * @param trips	the set of trips
	 * @param time1	the lower bound of the time horizon
	 * @param time2	the upper bound of the time horizon
	 * @return	the overlapping set of trips
	 */
	public Set<Trip> getTripsBetweenTimes(Collection<Trip> trips, LocalTime time1, LocalTime time2) {
		Set<Trip> setTrips = new HashSet<>();
		for (Trip trip : trips) {
			LocalTime tripDepartureTime = trip.departureTime();
			LocalTime tripArrivalTime = trip.arrivalTime();
			if (tripArrivalTime.compareTo(time2) <= 0
					&& tripDepartureTime.compareTo(time1) >= 0) {
				setTrips.add(trip);
			}
		}
		return setTrips;
	}
	
	/**
	 * @param trip	the trip
	 * @return	returns the counter for n_t corresponding to <code>trip</code>
	 */
//	public Counter getTripCounterN(Trip trip) {
//		Counter counter = tripToN.get(trip);
//		if (counter == null) {
//			counter = new Counter("n_t#" + trip.toString());
//			tripToN.put(trip, counter);
//		}
//		return counter;
//	}
	
	/**
	 * @param trip	the trip
	 * @return	returns the counter for b_t corresponding to <code>trip</code>
	 */
//	public Counter getTripCounterB(Trip trip) {
//		Counter counter = tripToB.get(trip);
//		if (counter == null) {
//			counter = new Counter("b_t#" + trip.toString());
//			tripToB.put(trip, counter);
//		}
//		return counter;
//	}
	
	private static class TrainWithKPI implements Comparable<TrainWithKPI> {
		private int trainNr;
		private double kpiNew;
		private double kpiOld;
		
		public TrainWithKPI(int trainNr, double kpiNew, double kpiOld) {
			this.trainNr = trainNr;
			this.kpiNew = kpiNew;
			this.kpiOld = kpiOld;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof TrainWithKPI))
				return false;
			TrainWithKPI o = (TrainWithKPI) other;
			return this.trainNr == o.trainNr &&
					this.kpiNew == o.kpiNew &&
					this.kpiOld == o.kpiOld;
		}
		
		@Override
		public int hashCode() {
			return 7*Integer.hashCode(trainNr) + 13*Double.hashCode(kpiNew) + 17*Double.hashCode(kpiOld);
		}

		@Override
		public int compareTo(TrainWithKPI o) {
			int res1 = Double.compare(this.kpiNew, o.kpiNew);
			if (res1 != 0)
				return res1;
			int res2 = Double.compare(this.kpiOld, o.kpiOld);
			if (res2 != 0)
				return res2;
			return Integer.compare(this.trainNr, o.trainNr);
		}
		
	}
	
	private static class JourneyWithKPI implements Comparable<JourneyWithKPI> {

		private Journey journey;
		private double kpiNew;
		private double kpiOld;

		public JourneyWithKPI(
				Journey journey, 
				double kpiNew, 
				double kpiOld) {
			this.journey = journey;
			this.kpiNew = kpiNew;
			this.kpiOld = kpiOld;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof JourneyWithKPI))
				return false;
			JourneyWithKPI o = (JourneyWithKPI) other;
			return this.journey.equals(o.journey) &&
					this.kpiNew == o.kpiNew &&
					this.kpiOld == o.kpiOld;
		}

		@Override
		public int hashCode() {
			return 7*journey.hashCode() + 13*Double.hashCode(kpiNew) + 17*Double.hashCode(kpiOld);
		}

		@Override
		public int compareTo(JourneyWithKPI o) {
			int res1 = Double.compare(this.kpiNew, o.kpiNew);
			if (res1 != 0)
				return res1;
			int res2 = Double.compare(this.kpiOld, o.kpiOld);
			if (res2 != 0)
				return res2;
			return journey.toString().compareTo(o.journey.toString());
		}

	}
}
