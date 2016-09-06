package wagon;

import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import wagon.algorithms.*;
import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.network.expanded.EventActivityNetwork;
import wagon.simulation.*;
import wagon.timetable.Timetable;

public class Main {
	
	public static void main(String[] args) {
//		LocalDateTime dateTime = LocalDateTime.of(2016, 1, 25, 16, 5, 14);
//		System.out.println(dateTime.format(DateTimeFormatter.ofPattern("ddMMMyyyy:HH:mm:ss", Locale.US)));
		try {
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/full_dataset2.xlsx", true);
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/smaller_sample_schedule1.xlsx", 2);
//			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/smaller_sample_schedule1_export.xml");
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
//			sample.export("data/materieelplan/processed/full_dataset2_export.xml");
//			sample.export("data/materieelplan/processed/smaller_sample_schedule1_day2_export.xml");
//			EventActivityNetwork network = EventActivityNetwork.createTransferNetwork(sample, 2, 1);
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork5();
			
//			BiCriterionDijkstra biDijkstra = new BiCriterionDijkstra(network, Criterion.DISTANCE, Criterion.TRANSFER);
//			BiCriterionDijkstra biDijkstra = new BiCriterionDijkstra(network, Criterion.TRANSFER, Criterion.DISTANCE);
//			Path path = biDijkstra.lexicographicallyFirst("rta", "ut", LocalTime.parse("08:10"));
//			System.out.println(path.toString());
			
			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160112.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
			options.setSeed(5678);
			options.setSegmentWidth(1); // in minutes
			options.setTransferTime(1);
			options.setNumberofProcessors(4);
			options.setPhi(1.0);
			options.setPsi(0.5);
//			
//			CiCoData cicoData = CiCoData
//					.importRawData(options);
//			cleanCiCoData(cicoData, sample);
//			cicoData.getJourneySummary();
//			cicoData.exportEmpiricalArrivalRateOfJourney(
//					"rta", 
//					"ut", 
//					5*60, 
//					"data/cico/export/20160209_rta_ut.csv");
//			cicoData.exportEmpiricalArrivalRateOfCheckInStation(
//					"rta", 
//					10*60, 
//					"data/cico/rta_20160209.csv");
//			
//			Collection<Passenger> selectedPassengers = cicoData.getPassengersAtCheckInStation("rtd");
//			PiecewiseConstantProcess arrivals = new PiecewiseConstantProcess(selectedPassengers, 5*60, 0);
//			arrivals.exportDrawsFromProcess(10*60, "data/cico/test.csv");
//			arrivals.exportArrivalRate("data/cico/rates_piecewise_constant.csv");
//			System.out.println("p: " + arrivals.kolmogorovSmirnovTest("matlab/ks_test_rtd_20160209.csv"));
			
//			Map<Journey, ArrivalProcess> arrivalProcesses = estimateArrivalProcesses(cicoData, options);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
//			RollingStockComposer rcomposer = new RollingStockComposerBasic(sample, rdata, 0);
			
//			long startTime = System.nanoTime();
//			SimModel sim = new SimModel(
//					sample, 
//					arrivalProcesses, 
//					cicoData, 
//					rcomposer, 
//					options);
//			Report report = sim.start();
//			System.out.println(report.summary());
//			System.out.println(report.reportBestAndWorstTrains());
//			long endTime = System.nanoTime();
//			double duration = (endTime - startTime)*1e-9;
//			System.out.println("Simulation took " + duration + " s");
			
			long startTime = System.nanoTime();
			ParallelSimModel parSim = new ParallelSimModel(
					sample, 
					rdata, 
					options);
			ParallelReport parReport = parSim.start(4);
			System.out.println(parReport.summary());
			System.out.println(parReport.reportWorstTrains());
			System.out.println(parReport.reportWorstJourneys());
			System.out.println(parReport.reportWorstOrigins());
			long endTime = System.nanoTime();
			double duration = (endTime-startTime)*1e-9;
			System.out.println("Simulation took " + duration + " s");
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (InvalidFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private static Map<Journey, ArrivalProcess> estimateArrivalProcesses(CiCoData cicoData, Options options) {
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
		long seed = 0;
		long count = 0;
		double maxLambda = Double.NEGATIVE_INFINITY;
		for (Journey journey : map.keySet()) {
			count++;
			Collection<Passenger> passengers = map.get(journey);
//			HybridArrivalProcess arrivalProcess = new HybridArrivalProcess(passengers, 0, Main.HORIZON, 5*60, seed);
			PiecewiseConstantProcess arrivalProcess = new PiecewiseConstantProcess(passengers, options.getSegmentWidth()*60, seed);
			resultMap.put(journey, arrivalProcess);
			double lambda = arrivalProcess.getLambdaUpperBound();
			if (lambda > maxLambda)
				maxLambda = lambda;
			seed++;
			System.out.println("journey " + count + "/" + map.keySet().size());
		}
		return resultMap;
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
//		log.info("Passengers before removal: " + cicoData.getPassengers().size());
//		log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
//		log.info("Passengers remaining: " + passengers.size());
		cicoData.setPassengers(passengers);
	}

}
