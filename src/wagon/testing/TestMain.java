package wagon.testing;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.simulation.Options;
import wagon.simulation.Passenger;
import wagon.timetable.Timetable;

public class TestMain {

	public static void main(String[] args) {
		try {
			Timetable timetable = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");

			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160112.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);
			options.setSeed(1234);
			options.setSegmentWidth(60); // in minutes
			options.setTransferTime(1);
			options.setNumberofProcessors(4);

			CiCoData cicoData = CiCoData.importRawData(options);
			cleanCiCoData(cicoData, timetable);
			
			int[] widths = new int[] {1, 3, 5, 10, 12, 15, 20, 30};

			ArrivalProcessTesting testing = new ArrivalProcessTesting(timetable, cicoData);
			double[] scores = testing.calculateLoss(widths);
			
			System.out.println(toString(scores));
		} catch(IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private static String toString(double[] array) {
		boolean first = true;
		String s = "[";
		for (double d : array) {
			if (first) {
				first = false;
			} else
				s += ", ";
			s += String.valueOf(d);
		}
		return s;
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
