package sandbox;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.data.CiCoData;
import wagon.infrastructure.Station;
import wagon.simulation.Options;
import wagon.simulation.Passenger;
import wagon.timetable.*;

public class InspectRushHourSandbox {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");

			Options options = new Options();
			options.setPathToCiCoData("data/cico/ritten_20160112.csv");
			options.setPathToStations("data/cico/omzettabel_stations.csv");
			options.setDayOfWeek(2);

			CiCoData cicoData = CiCoData
					.importRawData(options);
			cleanCiCoData(cicoData, sample);
			cicoData.getJourneySummary();
			
			// planned capacity
			Set<Trip> trips = sample.getAllTrips(2);
			Set<Trip> morningRushHour = getTripsMorningRushHour(trips);
			Set<Trip> afternoonRushHour = getTripsAfternoonRushHour(trips);
			
			// passenger arrivals
			int countMorning = 0;
			int countEvening = 0;
			for (Passenger passenger : cicoData.getPassengers()) {
				LocalTime arrivalTime = passenger.getCheckInTime().toLocalTime();
				if (arrivalTime.compareTo(LocalTime.parse("07:00")) >= 0 &&
						arrivalTime.compareTo(LocalTime.parse("09:00")) <= 0) {
					countMorning++;
				} else if (arrivalTime.compareTo(LocalTime.parse("16:00")) >= 0 &&
						arrivalTime.compareTo(LocalTime.parse("18:00")) <= 0) {
					countEvening++;
				}
			}
			
			System.out.println("Seats morning rush: " + (double)totalNumberOfSeats(morningRushHour)/morningRushHour.size());
			System.out.println("Morning arrivals: " + countMorning);
			System.out.println("Seats evening rush: " + (double)totalNumberOfSeats(afternoonRushHour)/afternoonRushHour.size());
			System.out.println("Evening arrivals: " + countEvening);
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
	}
	
	private static int totalNumberOfSeats(Collection<Trip> trips) {
		int nrSeats = 0;
		for (Trip trip : trips) {
			nrSeats += trip.getTrainService().getAllSeats();
		}
		return nrSeats;
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
	
	private static Set<Trip> getTripsMorningRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("07:00"), LocalTime.parse("09:00"));
	}
	
	private static Set<Trip> getTripsAfternoonRushHour(Collection<Trip> trips) {
		return getTripsBetweenTimes(trips, LocalTime.parse("16:00"), LocalTime.parse("18:00"));
	}
	
	private static Set<Trip> getTripsBetweenTimes(Collection<Trip> trips, LocalTime time1, LocalTime time2) {
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
}