package wagon.simulation;

import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;

import wagon.timetable.*;

public class ParallelReport {

	private Set<Report> reports;
	private Set<ScheduledTrip> trips;
	
	public ParallelReport(
			Collection<Report> reports, 
			Set<ScheduledTrip> trips) {
		this.reports = new HashSet<>(reports);
		this.trips = trips;
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
	
	public String reportBestAndWorstTrains() {
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
			double val = report.calculateKPIOld(trips);
			kpi[count] = val;
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
			double val = report.calculateKPINew(trips);
			kpi[count] = val;
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
	
	private static class KPIEstimate implements Comparable<KPIEstimate> {
		public final double mean;
		public final double std;
		
		public KPIEstimate(double mean, double std) {
			this.mean = mean;
			this.std = std;
		}
		
		@Override
		public String toString() {
			return mean + " (" + std + ")";
		}
		
		@Override
		public int hashCode() {
			return 13*Double.hashCode(mean) + 39*Double.hashCode(std);
		}

		@Override
		public int compareTo(KPIEstimate o) {
			int res1 = Double.compare(this.mean, o.mean);
			if (res1 != 0)
				return res1;
			return Double.compare(this.std, o.std);
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
