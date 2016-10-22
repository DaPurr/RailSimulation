package wagon.simulation;

import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;

import wagon.infrastructure.Station;
import wagon.timetable.*;

public class ParallelReport {
	
	private final double cicoCorrectionFactor = 1.215767122505321;

	private Set<Report> reports;
	private Set<Trip> trips;
	private Map<Journey, Set<Trip>> journeyTrips;
	private Map<Station, Set<Trip>> originTrips;
	
	public ParallelReport(
			Collection<Report> reports, 
			Set<Trip> trips, 
			Map<Journey, Set<Trip>> journeyTrips, 
			Map<Station, Set<Trip>> originTrips) {
		this.reports = new HashSet<>(reports);
		this.trips = trips;
		this.journeyTrips = journeyTrips;
		this.originTrips = originTrips;
	}
	
	public String reportWorstJourneys() {
		List<JourneyWithKPI> journeyList = new ArrayList<>();
		for (Entry<Journey, Set<Trip>> entry : journeyTrips.entrySet()) {
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
	
	public String reportWorstOrigins() {
		List<OriginWithKPI> originList = new ArrayList<>();
		for (Entry<Station, Set<Trip>> entry : originTrips.entrySet()) {
 			Station origin = entry.getKey();
			KPIEstimate kpiNew = calculateKPINew(entry.getValue());
			KPIEstimate kpiOld = calculateKPIOld(entry.getValue());
			originList.add(new OriginWithKPI(origin, kpiNew, kpiOld));
		}
		
		Collections.sort(originList);
		
		// report worst 15
		String s = "";
		s += System.lineSeparator();
		s += "WORST 10 ORIGINS" + System.lineSeparator();
		s += "===================" + System.lineSeparator();
		for (int i = 0; i < 15; i++) {
			if (i >= originList.size())
				break;
			OriginWithKPI originKPI = originList.get(i);
			s += originKPI.origin.name() + ": KPI_{new}=" + originKPI.kpiNew + "\tKPI_{old}=" + originKPI.kpiOld + System.lineSeparator();
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
		Set<Trip> tripsMorningRush = getTripsMorningRushHour(trips);
		s += "MORNING RUSH HOUR" + System.lineSeparator();
		s += "=========================" + System.lineSeparator();
		s += "KPI_{old}=" + calculateKPIOld(tripsMorningRush) + System.lineSeparator();
		s += "KPI_{new}=" + calculateKPINew(tripsMorningRush) + System.lineSeparator();
		s += System.lineSeparator();
		Set<Trip> tripsAfternoonRush = getTripsAfternoonRushHour(trips);
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
	
	public String reportWorstTrains() {
		Map<Integer, Collection<Trip>> trainMap = new HashMap<>();
		
		// add trips to all train numbers
		for (Trip trip : trips) {
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
	
	public String reportOrderedStatistics(int n) {
		Map<Integer, Collection<Trip>> trainMap = new HashMap<>();
		
		// add trips to all train numbers
		for (Trip trip : trips) {
			int trainNr = trip.getTrainService().id();
			Collection<Trip> collection = trainMap.get(trainNr);
			if (collection == null) {
				collection = new ArrayList<>();
				trainMap.put(trainNr, collection);
			}
			collection.add(trip);
		}
		
		List<List<Double>> orderedLists = new ArrayList<>();
		for (Report report : reports) {
			List<Double> listKPI = new ArrayList<>();
			for (Entry<Integer, Collection<Trip>> entry : trainMap.entrySet()) {
				double kpiNew = calculateKPINewAuxiliary(report, entry.getValue());
				listKPI.add(kpiNew);
			}
			Collections.sort(listKPI);
			orderedLists.add(listKPI);
		}
		
		List<KPIEstimate> orderedListEstimate = new ArrayList<>();
		for (int i = 0; i < orderedLists.get(0).size(); i++) {
			List<Double> tempList = new ArrayList<>();
			for (List<Double> list : orderedLists) {
				tempList.add(list.get(i));
			}
			double mean = mean(tempList);
			double std = std(tempList);
			KPIEstimate kpi = new KPIEstimate(mean, std);
			orderedListEstimate.add(kpi);
		}
		
		// report worst 10
		String s = "";
		s += System.lineSeparator();
		s += "WORST 10 TRAINS" + System.lineSeparator();
		s += "===============" + System.lineSeparator();
		for (int i = 0; i < n; i++) {
			if (i >= orderedListEstimate.size())
				break;
			KPIEstimate trainKPI = orderedListEstimate.get(i);
			s += (i+1) + ": KPI_{new}=" + trainKPI + System.lineSeparator();
		}
		return s;
	}
	
	public double calculateKPINewAuxiliary(Report report, Collection<Trip> trips) {
		double sumF = 0.0;
		double sumB = 0.0;
		for (Trip trip : trips) {
			TripCounters tripCounters = report.tripToCounters.get(trip);
			if (tripCounters == null) {
//				throw new IllegalArgumentException("Counters for trip cannot be found.");
				continue;
			}
			double countB = tripCounters.getB();
			countB *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
			double countN = tripCounters.getN();
			countN *= cicoCorrectionFactor;
			double seats = tripCounters.getSeatCapacity();
//			seats *= (2-cicoCorrectionFactor); // apply CiCo correction factor to capacity
			double seatsAvailable = Math.max(seats - (countN - countB), 0.0);
			double countF = Math.min(seatsAvailable, countB);
			sumF += countF;
			sumB += countB;
		}
		double kpi = sumF/sumB;
		return kpi;
	}
	
	public double calculateKPIOldAuxiliary(Report report, Collection<Trip> trips) {
		double numerator = 0.0;
		double denominator = 0.0;
		for (Trip trip : trips) {
			TripCounters tripCounters = report.tripToCounters.get(trip);
			if (tripCounters == null) {
//				throw new IllegalArgumentException("Counters for trip cannot be found.");
				continue;
			}
			double countN = tripCounters.getN();
			countN *= cicoCorrectionFactor; // apply CiCo correction factor to capacity
			double normCapacity = tripCounters.getNormCapacity();
//			normCapacity *= (2-cicoCorrectionFactor); // apply CiCo correction factor to capacity
			numerator += countN*Math.min(normCapacity/countN, 1);
			denominator += countN;
		}
		double kpi = numerator/denominator;
		return kpi;
	}
	
	public KPIEstimate calculateKPIOld(Collection<Trip> trips) {
		double[] kpi = new double[reports.size()];
		int count = 0;
		for (Report report : reports) {
			kpi[count] = calculateKPIOldAuxiliary(report, trips);
			count++;
		}
		double mean = mean(kpi);
		double std = std(kpi);
		return new KPIEstimate(mean, std);
	}
	
	public KPIEstimate calculateKPINew(Collection<Trip> trips) {
		double[] kpi = new double[reports.size()];
		int count = 0;
		for (Report report : reports) {
			kpi[count] = calculateKPINewAuxiliary(report, trips);
			count++;
		}
		
		double mean = mean(kpi);
		double std = std(kpi);
		
		return new KPIEstimate(mean, std);
	}
	
	public Set<Trip> getTripsMorningRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("07:00"), LocalTime.parse("09:00"));
	}
	
	public Set<Trip> getTripsAfternoonRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("16:00"), LocalTime.parse("18:00"));
	}
	
	public Set<Trip> getTripsFromTrain(int trainNumber) {
		Set<Trip> set = new LinkedHashSet<>();
		for (Trip trip : trips) {
			if (trip.getTrainService().id() == trainNumber)
				set.add(trip);
		}
		return set;
	}
	
	public Set<Trip> getTripsBetweenTimes(
			Collection<Trip> trips, 
			LocalTime time1, 
			LocalTime time2) {
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
	
	private double mean(double[] vals) {
		if (vals.length < 1)
			throw new IllegalArgumentException("Array length cannot be smaller than 1");
		double sum = 0.0;
		for (double val : vals) {
			sum += val;
		}
		return sum/vals.length;
	}
	
	private double mean(Collection<Double> vals) {
		if (vals.size() < 1)
			throw new IllegalArgumentException("Array length cannot be smaller than 1");
		double sum = 0.0;
		for (double val : vals) {
			sum += val;
		}
		return sum/vals.size();
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
		return Math.sqrt(sum/(vals.length-1));
	}
	
	private double std(Collection<Double> vals) {
		if (vals.size() <= 1)
			return 0;
		double sum = 0.0;
		double mean = mean(vals);
		for (double val : vals) {
			double term = mean-val;
			sum += term*term;
		}
		return Math.sqrt(sum/(vals.size()-1));
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
	
	private static class OriginWithKPI implements Comparable<OriginWithKPI> {
		
		private Station origin;
		private KPIEstimate kpiNew;
		private KPIEstimate kpiOld;
		
		public OriginWithKPI(
				Station origin, 
				KPIEstimate kpiNew, 
				KPIEstimate kpiOld) {
			this.origin = origin;
			this.kpiNew = kpiNew;
			this.kpiOld = kpiOld;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof OriginWithKPI))
				return false;
			OriginWithKPI o = (OriginWithKPI) other;
			return this.origin.equals(o.origin) &&
					this.kpiNew.equals(o.kpiNew) &&
					this.kpiOld.equals(o.kpiOld);
		}
		
		@Override
		public int hashCode() {
			return 7*origin.hashCode() + 13*kpiNew.hashCode() + 17*kpiOld.hashCode();
		}

		@Override
		public int compareTo(OriginWithKPI o) {
			int res1 = this.kpiNew.compareTo(o.kpiNew);
			if (res1 != 0)
				return res1;
			int res2 = this.kpiOld.compareTo(o.kpiOld);
			if (res2 != 0)
				return res2;
			return origin.toString().compareTo(o.origin.toString());
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
