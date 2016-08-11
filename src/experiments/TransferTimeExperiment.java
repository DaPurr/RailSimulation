package experiments;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.*;
import wagon.infrastructure.Station;
import wagon.simulation.*;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

public class TransferTimeExperiment {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset_export.xml");
			
			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160209.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
//			options.setSeed(0);
			options.setSegmentWidth(5); // needs to divide 60
			
			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			int LB = 0;
			int UB = 8;
			int step = 1;
			
			List<Double> kpi = new ArrayList<>();
			List<Double> times = new ArrayList<>();
			
			for (int t = LB; t <= UB; t += step) {
				options.setTransferTime(t);
				long startTime = System.nanoTime();
				ParallelSimModel parSim = new ParallelSimModel(
						sample, 
						rdata, 
						options);
				ParallelReport parReport = parSim.start(4);
				long endTime = System.nanoTime();
				double duration = (endTime-startTime)*1e-9;
				
				times.add(duration);
				
				Collection<ScheduledTrip> tripsRushHour = parReport
						.getTripsMorningRushHour(sample.getAllTrips(options.getDayOfWeek()));
				Collection<ScheduledTrip> tripsEveningRushHour = parReport
						.getTripsAfternoonRushHour(sample.getAllTrips(options.getDayOfWeek()));
				tripsRushHour.addAll(tripsEveningRushHour);
				kpi.add(parReport.calculateKPINew(tripsRushHour).mean);
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
		cicoData.setPassengers(passengers);
	}

}
