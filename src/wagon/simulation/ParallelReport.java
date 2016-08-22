package wagon.simulation;

import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import wagon.timetable.*;

public class ParallelReport {
	
	private final double cicoCorrectionFactor = 1.215767122505321;

	private Set<Report> reports;
	private Set<ScheduledTrip> trips;
	private Map<Journey, Set<ScheduledTrip>> journeyTrips;
	
	public ParallelReport(
			Collection<Report> reports, 
			Set<ScheduledTrip> trips, 
			Map<Journey, Set<ScheduledTrip>> journeyTrips) {
		this.reports = new HashSet<>(reports);
		this.trips = trips;
		this.journeyTrips = journeyTrips;
	}
	
	public String reportWorstJourneys() {
		List<JourneyWithKPI> journeyList = new ArrayList<>();
		for (Entry<Journey, Set<ScheduledTrip>> entry : journeyTrips.entrySet()) {
 			Journey journey = entry.getKey();
			KPIEstimate kpiNew = calculateKPINew(entry.getValue());
			KPIEstimate kpiOld = calculateKPIOld(entry.getValue());
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
	
	public String summary() {
		String s = "";
		s += "ALL TRIPS" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(trips) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(trips) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsMorningRush = getTripsMorningRushHour(trips);
		s += "MORNING RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsMorningRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsMorningRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsAfternoonRush = getTripsAfternoonRushHour(trips);
		s += "AFTERNOON RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAfternoonRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAfternoonRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<ScheduledTrip> tripsAllRush = new HashSet<>(tripsMorningRush);
		tripsAllRush.addAll(tripsAfternoonRush);
		s += "COMBINED RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsAllRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsAllRush) + System.lineSeparator();
		s += System.lineSeparator();
		
		return s;
	}
	
	public String reportWorstTrains() {
		Map<Integer, Collection<ScheduledTrip>> trainMap = new HashMap<>();
		
		// add trips to all train numbers
		for (ScheduledTrip trip : trips) {
			int trainNr = trip.getTrainService().id();
			Collection<ScheduledTrip> collection = trainMap.get(trainNr);
			if (collection == null) {
				collection = new ArrayList<>();
				trainMap.put(trainNr, collection);
			}
			collection.add(trip);
		}
		
		List<TrainWithKPI> trainList = new ArrayList<>();
		for (Entry<Integer, Collection<ScheduledTrip>> entry : trainMap.entrySet()) {
			int trainNr = entry.getKey();
			KPIEstimate kpiNew = calculateKPINew(entry.getValue());
			KPIEstimate kpiOld = calculateKPIOld(entry.getValue());
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
	
	public KPIEstimate calculateKPIOld(Collection<ScheduledTrip> trips) {
		double[] kpi = new double[reports.size()];
		int count = 0;
		for (Report report : reports) {
			double numerator = 0.0;
			double denominator = 0.0;
			for (ScheduledTrip trip : trips) {
				Counter counterN = report.getTripCounterN(trip);
				if (counterN == null)
					throw new IllegalArgumentException("Counters for trip cannot be found.");
				double countN = counterN.getValue();
				countN *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
				double normCapacity = trip.getTrainService().normCapacity2(trip.getNorm());
				normCapacity += trip.getTrainService().normCapacity1(trip.getNorm());
//				normCapacity *= (2-cicoCorrectionFactor); // apply CiCo correction factor to capacity
				numerator += countN*Math.min(normCapacity/countN, 1);
				denominator += countN;
			}
			kpi[count] = numerator/denominator;
			count++;
		}
		double mean = mean(kpi);
		double std = std(kpi);
		return new KPIEstimate(mean, std);
	}
	
	public KPIEstimate calculateKPINew(Collection<ScheduledTrip> trips) {
		double[] kpi = new double[reports.size()];
		int count = 0;
		for (Report report : reports) {
			double sumF = 0.0;
			double sumB = 0.0;
			for (ScheduledTrip trip : trips) {
				Counter counterN = report.getTripCounterN(trip);
	 			Counter counterB = report.getTripCounterB(trip);
				if (counterN == null || counterB == null)
					throw new IllegalArgumentException("Counters for trip cannot be found.");
				double countB = counterB.getValue();
				countB *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
				double countN = counterN.getValue();
				countN *= cicoCorrectionFactor;
				double seats = trip.getTrainService().getSeats2() + trip.getTrainService().getFoldableSeats();
				seats += trip.getTrainService().getSeats1();
//				seats *= (2-cicoCorrectionFactor); // apply CiCo correction factor to capacity
				double seatsAvailable = Math.max(seats - (countN - countB), 0.0);
				double countF = Math.min(seatsAvailable, countB);
				sumF += countF;
				sumB += countB;
			}
			kpi[count] = sumF/sumB;
			count++;
		}
		
		double mean = mean(kpi);
		double std = std(kpi);
		
		return new KPIEstimate(mean, std);
	}
	
	public Set<ScheduledTrip> getTripsMorningRushHour(Collection<ScheduledTrip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("07:00"), LocalTime.parse("09:00"));
	}
	
	public Set<ScheduledTrip> getTripsAfternoonRushHour(Collection<ScheduledTrip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("16:00"), LocalTime.parse("18:00"));
	}
	
	public Set<ScheduledTrip> getTripsFromTrain(int trainNumber, Collection<ScheduledTrip> trips) {
		Set<ScheduledTrip> set = new HashSet<>();
		for (ScheduledTrip trip : trips) {
			if (trip.getTrainService().id() == trainNumber)
				set.add(trip);
		}
		return set;
	}
	
	public Set<ScheduledTrip> getTripsBetweenTimes(
			Collection<ScheduledTrip> trips, 
			LocalTime time1, 
			LocalTime time2) {
		Set<ScheduledTrip> setTrips = new HashSet<>();
		for (ScheduledTrip trip : trips) {
			LocalTime tripDepartureTime = trip.departureTime();
			LocalTime tripArrivalTime = trip.arrivalTime();
			if (tripArrivalTime.compareTo(time2) <= 0
					&& tripDepartureTime.compareTo(time1) >= 0) {
				setTrips.add(trip);
			}
		}
		return setTrips;
	}
	
	private double mean(double[] vals) {
		if (vals.length < 1)
			throw new IllegalArgumentException("Array length cannot be smaller than 1");
		double sum = 0.0;
		for (double val : vals) {
			sum += val;
		}
		return sum/vals.length;
	}
	
	private double std(double[] vals) {
		if (vals.length <= 1)
			return 0;
		double sum = 0.0;
		double mean = mean(vals);
		for (double val : vals) {
			double term = mean-val;
			sum += term*term;
		}
		return sum/(vals.length-1);
	}
	
	private static class JourneyWithKPI implements Comparable<JourneyWithKPI> {
		
		private Journey journey;
		private KPIEstimate kpiNew;
		private KPIEstimate kpiOld;
		
		public JourneyWithKPI(
				Journey journey, 
				KPIEstimate kpiNew, 
				KPIEstimate kpiOld) {
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
					this.kpiNew.equals(o.kpiNew) &&
					this.kpiOld.equals(o.kpiOld);
		}
		
		@Override
		public int hashCode() {
			return 7*journey.hashCode() + 13*kpiNew.hashCode() + 17*kpiOld.hashCode();
		}

		@Override
		public int compareTo(JourneyWithKPI o) {
			int res1 = this.kpiNew.compareTo(o.kpiNew);
			if (res1 != 0)
				return res1;
			int res2 = this.kpiOld.compareTo(o.kpiOld);
			if (res2 != 0)
				return res2;
			return journey.toString().compareTo(o.journey.toString());
		}
		
	}
	
	private static class TrainWithKPI implements Comparable<TrainWithKPI> {
		private int trainNr;
		private KPIEstimate kpiNew;
		private KPIEstimate kpiOld;
		
		public TrainWithKPI(
				int trainNr, 
				KPIEstimate kpiNew, 
				KPIEstimate kpiOld) {
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
					this.kpiNew.equals(o.kpiNew) &&
					this.kpiOld.equals(o.kpiOld);
		}
		
		@Override
		public int hashCode() {
			return 7*Integer.hashCode(trainNr) + 13*kpiNew.hashCode() + 17*kpiOld.hashCode();
		}

		@Override
		public int compareTo(TrainWithKPI o) {
			int res1 = this.kpiNew.compareTo(o.kpiNew);
			if (res1 != 0)
				return res1;
			int res2 = this.kpiOld.compareTo(o.kpiOld);
			if (res2 != 0)
				return res2;
			return Integer.compare(this.trainNr, o.trainNr);
		}
		
	}
}
