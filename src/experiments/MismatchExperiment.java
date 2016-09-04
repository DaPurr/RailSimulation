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
import wagon.simulation.*;
import wagon.timetable.*;

public class MismatchExperiment {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
			
			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160112.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
//			options.setSeed(5678);
			options.setTransferTime(1);
			options.setSegmentWidth(1); // in minutes
			options.setNumberofProcessors(4);
			
			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			double[] phis = new double[] {0.0, 0.2, 0.4, 0.6, 0.8, 1.0};
			
			List<Double> kpi = new ArrayList<>();
			List<Double> times = new ArrayList<>();
			
			List<Double> kpi14869 = new ArrayList<>();
			List<Double> kpi2825 = new ArrayList<>();
			List<Double> kpi14867 = new ArrayList<>();
			List<Double> kpi14871 = new ArrayList<>();
			List<Double> kpi2827 = new ArrayList<>();
			List<Double> kpi5663 = new ArrayList<>();
			List<Double> kpi4919 = new ArrayList<>();
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
				ParallelSimModel parSim = new ParallelSimModel(
						sample, 
						rdata, 
						arrivalProcesses, 
						options);
				ParallelReport parReport = parSim.start(32);
//				System.out.println(parReport.summary());
//				System.out.println(parReport.reportWorstTrains());
//				System.out.println(parReport.reportWorstJourneys());
				long endTime = System.nanoTime();
				double duration = (endTime-startTime)*1e-9;
				System.out.println("Simulation took " + duration + " s");
				
				times.add(duration);
				
				// individual train services
				KPIEstimate kpi_14869 = parReport.calculateKPINew(parReport.getTripsFromTrain(14869));
				kpi14869.add(kpi_14869.mean);
				KPIEstimate kpi_2825 = parReport.calculateKPINew(parReport.getTripsFromTrain(2825));
				kpi2825.add(kpi_2825.mean);
				KPIEstimate kpi_14867 = parReport.calculateKPINew(parReport.getTripsFromTrain(14867));
				kpi14867.add(kpi_14867.mean);
				KPIEstimate kpi_14871 = parReport.calculateKPINew(parReport.getTripsFromTrain(14871));
				kpi14871.add(kpi_14871.mean);
				KPIEstimate kpi_2827 = parReport.calculateKPINew(parReport.getTripsFromTrain(2827));
				kpi2827.add(kpi_2827.mean);
				KPIEstimate kpi_5663 = parReport.calculateKPINew(parReport.getTripsFromTrain(5663));
				kpi5663.add(kpi_5663.mean);
				KPIEstimate kpi_4919 = parReport.calculateKPINew(parReport.getTripsFromTrain(4919));
				kpi4919.add(kpi_4919.mean);
				KPIEstimate kpi_5020 = parReport.calculateKPINew(parReport.getTripsFromTrain(5020));
				kpi5020.add(kpi_5020.mean);
				KPIEstimate kpi_5618 = parReport.calculateKPINew(parReport.getTripsFromTrain(5618));
				kpi5618.add(kpi_5618.mean);
				KPIEstimate kpi_2218 = parReport.calculateKPINew(parReport.getTripsFromTrain(2218));
				kpi2218.add(kpi_2218.mean);
				
				File file = new File("data/output_"+count+"_"+phi+".txt");
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(parReport.summary()); bw.newLine();
				bw.write(parReport.reportWorstTrains()); bw.newLine();
				bw.write(parReport.reportWorstJourneys()); bw.newLine();
				bw.write(parReport.reportWorstOrigins()); bw.newLine();
				bw.newLine();
				
				bw.write("14869: KPI=" + kpi_14869.mean + " (" + kpi_14869.std + ")"); bw.newLine();
				bw.write("2825: KPI=" + kpi_2825.mean + " (" + kpi_2825.std + ")"); bw.newLine();
				bw.write("14867: KPI=" + kpi_14867.mean + " (" + kpi_14867.std + ")"); bw.newLine();
				bw.write("14871: KPI=" + kpi_14871.mean + " (" + kpi_14871.std + ")"); bw.newLine();
				bw.write("2827: KPI=" + kpi_2827.mean + " (" + kpi_2827.std + ")"); bw.newLine();
				bw.write("5663: KPI=" + kpi_5663.mean + " (" + kpi_5663.std + ")"); bw.newLine();
				bw.write("4919: KPI=" + kpi_4919.mean + " (" + kpi_4919.std + ")"); bw.newLine();
				bw.write("5020: KPI=" + kpi_5020.mean + " (" + kpi_5020.std + ")"); bw.newLine();
				bw.write("5618: KPI=" + kpi_5618.mean + " (" + kpi_5618.std + ")"); bw.newLine();
				bw.write("2218: KPI=" + kpi_2218.mean + " (" + kpi_2218.std + ")"); bw.newLine();
				
				bw.close();
				count++;
			}
			
			System.out.println("Times: " + times.toString());
			System.out.println("KPI_{new}: " + kpi.toString());
			
			System.out.println("14869: " + kpi14869);
			System.out.println("2825: " + kpi2825);
			System.out.println("14867: " + kpi14867);
			System.out.println("14871: " + kpi14871);
			System.out.println("2827: " + kpi2827);
			System.out.println("5663: " + kpi5663);
			System.out.println("4919: " + kpi4919);
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