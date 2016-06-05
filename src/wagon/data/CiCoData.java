package wagon.data;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import wagon.algorithms.DefaultPath;
import wagon.algorithms.RouteGeneration;
import wagon.algorithms.RouteSelection;
import wagon.algorithms.SLTLARouteSelection;
import wagon.infrastructure.Station;
import wagon.simulation.Passenger;
import wagon.simulation.PassengerGroup;

public class CiCoData {

	private Set<Passenger> passengers;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	private CiCoData() {
		passengers = new LinkedHashSet<>();
	}
	
	/**
	 * @return	the set of passengers
	 */
	public Set<Passenger> getPassengers() {
		return new LinkedHashSet<>(passengers);
	}
	
	/**
	 * Replaces the current set of passengers with <code>passengers</code>.
	 * 
	 * @param passengers	the new set of passengers
	 */
	public void setPassengers(Set<Passenger> passengers) {
		this.passengers = passengers;
	}
	
	public static CiCoData importRawData(	String cicoFileName,
											String stationTableFileName
											) throws IOException {
		if (!cicoFileName.matches(".*\\.csv") ||
				!stationTableFileName.matches(".*\\.csv"))
			throw new IllegalArgumentException("File needs to be CSV format.");
		
		CiCoData cicoData = new CiCoData();
		
		Map<Integer, String> stationCodeToName = new LinkedHashMap<>();
		File file = new File(stationTableFileName);
		BufferedReader br = new BufferedReader(
				new FileReader(file));
		String line = br.readLine();
		line = br.readLine();
		cicoData.log.info("Start reading station conversion table ...");
		while (line != null) {
			String[] parts = line.split(",");
			stationCodeToName.put(Integer.parseInt(parts[0]), 
					parts[1].toLowerCase());
			line = br.readLine();
		}
		br.close();
		cicoData.log.info("... Finish reading station conversion table");
		
		file = new File(cicoFileName);
		br = new BufferedReader(
				new FileReader(file));
		line = br.readLine();
		line = br.readLine();
		cicoData.log.info("Start reading CiCo data from location: " + cicoFileName + " ...");
		long counter = 0;
		while (line != null) {
			counter++;
			String[] parts = line.split(",");
			LocalDateTime checkInTime = toLocalDateTimeObject(parts[10]);
			LocalDateTime checkOutTime = toLocalDateTimeObject(parts[27]);
			// apply correction of 3 mins
			checkOutTime = checkOutTime.plusMinutes(3);
			if (checkInTime.compareTo(checkOutTime) > 0)
				checkOutTime = checkOutTime.plusDays(1);
			
			// only accept passengers inside the time frame 06:00 - 20:00
			if (!(checkInTime.compareTo(LocalDateTime.of(2016, 4, 11, 6, 0)) > 0 &&
					checkOutTime.compareTo(LocalDateTime.of(2016, 4, 11, 6, 0)) > 0 &&
					checkOutTime.compareTo(LocalDateTime.of(2016, 4, 11, 20, 0)) < 0 &&
					checkInTime.compareTo(LocalDateTime.of(2016, 4, 11, 20, 0)) < 0)) {
				line = br.readLine();
				continue;
			}
			
			Station fromStation = toStationObject(parts[11], stationCodeToName);
			Station toStation = toStationObject(parts[28], stationCodeToName);
			Passenger passenger = new Passenger(checkInTime, 
					checkOutTime, 
					fromStation, 
					toStation);
			cicoData.passengers.add(passenger);
			
			if (counter % 10000 == 0)
				cicoData.log.info("... Already processed " + counter + " passengers");
			
			line = br.readLine();
		}
		br.close();
		cicoData.log.info("... Finish processing " + counter + " passenger check-ins and check-outs");
		
		return cicoData;
	}
	
	public List<PassengerGroup> processPassengersIntoGroups(RouteGeneration generator) {
		List<PassengerGroup> groups = new ArrayList<>();
		Multiset<DefaultPath> pathsMultiset = HashMultiset.create();
		long counter = 0;
		log.info("Begin processing CiCo data into passenger groups ...");
		for (Passenger passenger : passengers) {
			counter++;
			List<DefaultPath> paths = generator.generateRoutes(passenger.getFromStation().name(),
					passenger.getToStation().name(), 
					passenger.getCheckInTime(), 
					passenger.getCheckOutTime());
			RouteSelection selector = new SLTLARouteSelection(passenger.getCheckInTime(), 
					passenger.getCheckOutTime(), 
					10);
			DefaultPath path = selector.selectPath(paths);
			if (path != null)
				pathsMultiset.add(path);
			
			if (counter % 1000 == 0)
				log.info("... Finish selecting routes for " + counter + " passengers");
		}
		
		// now create the passenger groups with groups size
		for (DefaultPath path : pathsMultiset.elementSet()) {
			PassengerGroup group = new PassengerGroup(path, pathsMultiset.count(path));
			groups.add(group);
		}
		log.info("... Finish processing CiCo data into passenger groups");
		
		return groups;
	}
	
	private static LocalDateTime toLocalDateTimeObject(String text) {
//		LocalDateTime date = LocalDateTime.parse(text.toLowerCase(), DateTimeFormatter.ofPattern("ddMMMyyyy:HH:mm:ss"));
		LocalDateTime date = LocalDateTime.parse("2016-04-11T" + text.substring(text.length()-8, text.length()-3));
//		date = date.withYear(2016).withMonth(4).withDayOfMonth(11);
		return date;
	}
	
	private static Station toStationObject(String text,
			Map<Integer, String> conversionTable) {
		String stationName = conversionTable.get(Integer.parseInt(text));
		if (stationName == null)
			throw new IllegalStateException("Station name cannot be null");
		return new Station(stationName);
	}
}
