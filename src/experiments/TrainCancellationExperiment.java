package experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.simulation.*;
import wagon.timetable.*;

public class TrainCancellationExperiment {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
			
			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160112.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
//			options.setSeed(0);
			options.setTransferTime(1);
			options.setNumberofProcessors(4);
			options.setPhi(1.0);
			options.setSegmentWidth(1); // needs to divide 60
			
			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			double[] psis = new double[] {0.002, 0.005, 0.01, 0.015, 0.02, 0.025, 0.03};
			
			List<Double> kpi = new ArrayList<>();
			List<Double> times = new ArrayList<>();
			
			for (double psi : psis) {
				options.setPsi(psi);
				long startTime = System.nanoTime();
				ParallelSimModel parSim = new ParallelSimModel(
						sample, 
						rdata, 
						options);
				ParallelReport parReport = parSim.start(32);
				long endTime = System.nanoTime();
				double duration = (endTime-startTime)*1e-9;
				
				times.add(duration);
				
				Collection<Trip> tripsRushHour = parReport
						.getTripsMorningRushHour(sample.getAllTrips(options.getDayOfWeek()));
				Collection<Trip> tripsEveningRushHour = parReport
						.getTripsAfternoonRushHour(sample.getAllTrips(options.getDayOfWeek()));
				tripsRushHour.addAll(tripsEveningRushHour);
				kpi.add(parReport.calculateKPINew(tripsRushHour).mean);
				
				File file = new File("data/output_"+psi+".txt");
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(parReport.summary()); bw.newLine();
				bw.write(parReport.reportWorstTrains()); bw.newLine();
				bw.write(parReport.reportWorstJourneys()); bw.newLine();
				bw.write(parReport.reportWorstOrigins()); bw.newLine();
				bw.newLine();
				bw.close();
			}
			
			System.out.println("Times: " + times.toString());
			System.out.println("KPI_{new}: " + kpi.toString());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
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
//		log.info("Passengers before removal: " + cicoData.getPassengers().size());
//		log.info("Passengers removed with origin/destination not in timetable: " + passengersToDelete.size());
//		log.info("Passengers remaining: " + passengers.size());
		cicoData.setPassengers(passengers);
	}

}