package experiments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.CiCoData;
import wagon.data.RealisationData;
import wagon.infrastructure.Station;
import wagon.simulation.Options;
import wagon.simulation.ParallelReport;
import wagon.simulation.ParallelSimModel;
import wagon.simulation.Passenger;
import wagon.timetable.ScheduledTrip;
import wagon.timetable.Timetable;

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
			options.setSegmentWidth(5); // in minutes
			
			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
			
			double[] phis = new double[] {0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0};
			
			List<Double> kpi = new ArrayList<>();
			List<Double> times = new ArrayList<>();
			
			for (double phi : phis) {
				options.setPhi(phi);
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
