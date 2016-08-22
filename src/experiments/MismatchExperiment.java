package experiments;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.xml.sax.SAXException;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.rollingstock.*;
import wagon.simulation.*;
import wagon.timetable.*;

public class MismatchExperiment {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
			
			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160209.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
//			options.setSeed(0);
			options.setTransferTime(1);
			options.setSegmentWidth(1); // in minutes
			options.setNumberofProcessors(4);
			
			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			double[] phis = new double[] {0.0, 20.0, 40.0, 60.0, 80.0, 100.0};
			
			List<Double> kpi = new ArrayList<>();
			List<Double> times = new ArrayList<>();
			
			List<Double> kpi14869 = new ArrayList<>();
			List<Double> kpi2825 = new ArrayList<>();
			List<Double> kpi14867 = new ArrayList<>();
			List<Double> kpi2827 = new ArrayList<>();
			List<Double> kpi5663 = new ArrayList<>();
			List<Double> kpi14871 = new ArrayList<>();
			List<Double> kpi2654 = new ArrayList<>();
			List<Double> kpi5020 = new ArrayList<>();
			List<Double> kpi5618 = new ArrayList<>();
			List<Double> kpi2218 = new ArrayList<>();
			
			RandomGenerator random = new MersenneTwister();
			
			// estimate mismatch probabilities
			System.out.println("Begin estimating mismatch probabilities...");
			RollingStockComposerBasic rcomposer = new RollingStockComposerBasic(sample, rdata, random.nextLong());
			rcomposer = rcomposer.decreaseMismatches(options.getPhi());
			System.out.println("...Finish estimating mismatch probabilities");
			
			Map<Journey, ArrivalProcess> arrivalProcesses =  estimateArrivalProcesses(cicoData, options, random);
			
			int count = 1;
			for (double phi : phis) {
				options.setPhi(phi);
//				long startTime = System.nanoTime();
//				ParallelSimModel parSim = new ParallelSimModel(
//						sample, 
//						rdata, 
//						options);
//				ParallelReport parReport = parSim.start(4);
//				long endTime = System.nanoTime();
//				double duration = (endTime-startTime)*1e-9;
				
				long startTime = System.nanoTime();
				SimModel sim = new SimModel(
						sample, 
						arrivalProcesses, 
						cicoData, 
						rcomposer, 
						options);
				Report report = sim.start();
//				System.out.println(report.summary());
//				System.out.println(report.reportWorstTrains());
				long endTime = System.nanoTime();
				double duration = (endTime - startTime)*1e-9;
				System.out.println("Simulation took " + duration + " s");
				
				times.add(duration);
				
				// trips for individual trains
				Collection<ScheduledTrip> trips14869 = report.getTripsOfService(14869);
				kpi14869.add(report.calculateKPINew(trips14869));
				
				Collection<ScheduledTrip> trips2825 = report.getTripsOfService(2825);
				kpi2825.add(report.calculateKPINew(trips2825));
				
				Collection<ScheduledTrip> trips14867 = report.getTripsOfService(14867);
				kpi14867.add(report.calculateKPINew(trips14867));
				
				Collection<ScheduledTrip> trips2827 = report.getTripsOfService(2827);
				kpi2827.add(report.calculateKPINew(trips2827));
				
				Collection<ScheduledTrip> trips5663 = report.getTripsOfService(5663);
				kpi5663.add(report.calculateKPINew(trips5663));
				
				Collection<ScheduledTrip> trips14871 = report.getTripsOfService(14871);
				kpi14871.add(report.calculateKPINew(trips14871));
				
				Collection<ScheduledTrip> trips2654 = report.getTripsOfService(2654);
				kpi2654.add(report.calculateKPINew(trips2654));
				
				Collection<ScheduledTrip> trips5020 = report.getTripsOfService(5020);
				kpi5020.add(report.calculateKPINew(trips5020));
				
				Collection<ScheduledTrip> trips5618 = report.getTripsOfService(5618);
				kpi5618.add(report.calculateKPINew(trips5618));
				
				Collection<ScheduledTrip> trips2218 = report.getTripsOfService(2218);
				kpi2218.add(report.calculateKPINew(trips2218));
				
				Collection<ScheduledTrip> tripsRushHour = report
						.getTripsMorningRushHour(report.getAllTrips());
				Collection<ScheduledTrip> tripsEveningRushHour = report
						.getTripsAfternoonRushHour(report.getAllTrips());
				tripsRushHour.addAll(tripsEveningRushHour);
				kpi.add(report.calculateKPINew(tripsRushHour));
				
				File file = new File("data/output_"+count+"_"+phi+".txt");
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(report.summary()); bw.newLine();
				bw.write(report.reportWorstTrains()); bw.newLine();
				bw.write(report.reportWorstJourneys()); bw.newLine();
				bw.close();
				count++;
			}
			
			System.out.println("Times: " + times.toString());
			System.out.println("KPI_{new}: " + kpi.toString());
			
			System.out.println("14869: " + kpi14869);
			System.out.println("2825: " + kpi2825);
			System.out.println("14867: " + kpi14867);
			System.out.println("2827: " + kpi2827);
			System.out.println("5663: " + kpi5663);
			System.out.println("14871: " + kpi14871);
			System.out.println("2654: " + kpi2654);
			System.out.println("5020: " + kpi5020);
			System.out.println("5618: " + kpi5618);
			System.out.println("2218: " + kpi2218);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private static void cleanCiCoData(CiCoData cicoData, Timetable timetable) {
		Set<Passenger> passengers = cicoData.getPassengers();
		Set<Passenger> passengersToDelete = new LinkedHashSet<>();
		Set<Station> availableStations = timetable.getStations();

		// remove passengers with origin or destination not in timetable
		for (Passenger passenger : passengers) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			if (!availableStations.contains(from) || !availableStations.contains(to)) {
				passengersToDelete.add(passenger);
			}
		}
		passengers.removeAll(passengersToDelete);
		cicoData.setPassengers(passengers);
	}
	
	private static Map<Journey, ArrivalProcess> estimateArrivalProcesses(CiCoData cicoData, Options options, RandomGenerator random) {
		// group passengers based on their journeys
		Multimap<Journey, Passenger> map = LinkedHashMultimap.create();
		for (Passenger passenger : cicoData.getPassengers()) {
			Station from = passenger.getFromStation();
			Station to = passenger.getToStation();
			Journey journey = new Journey(from, to);
			map.put(journey, passenger);
		}
		
		// for each journey, estimate arrival process
		ConcurrentMap<Journey, ArrivalProcess> resultMap = new ConcurrentHashMap<>();
		long count = 0;
		for (Journey journey : map.keySet()) {
			count++;
			Collection<Passenger> passengers = map.get(journey);
			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, 24*60*60, 1*60, random.nextLong());
//			PiecewiseConstantProcess arrivalProcess = new PiecewiseConstantProcess(passengers, options.getSegmentWidth()*60, seed);
			resultMap.put(journey, arrivalProcess);
			System.out.println("journey " + count + "/" + map.keySet().size());
		}
		return resultMap;
	}

}
