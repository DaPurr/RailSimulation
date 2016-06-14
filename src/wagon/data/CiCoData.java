package wagon.data;

import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.JFrame;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;
import wagon.algorithms.DefaultPath;
import wagon.algorithms.RouteGeneration;
import wagon.algorithms.RouteSelection;
import wagon.algorithms.SLTLARouteSelection;
import wagon.infrastructure.Station;
import wagon.simulation.Options;
import wagon.simulation.Passenger;
import wagon.simulation.PassengerGroup;

public class CiCoData {

	private Set<Passenger> passengers;
	private Options options;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	private CiCoData() {
		this(null);
	}
	
	private CiCoData(Options options) {
		passengers = new LinkedHashSet<>();
		this.options = options;
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
	
	public static CiCoData importRawData(
			String cicoFileName,
			String stationTableFileName, 
			Options options) throws IOException {
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
			
			// apply correction of check-in and check-out
			if (options.getCheckInTimeCorrection() != 0) {
				checkInTime = checkInTime.plusMinutes(options.getCheckInTimeCorrection());
				cicoData.log.info("Check-in time adjusted by "
						+ options.getCheckInTimeCorrection() + " minutes...");
			}
			if (options.getCheckOutTimeCorrection() != 0) {
				checkOutTime = checkOutTime.plusMinutes(options.getCheckOutTimeCorrection());
				cicoData.log.info("Check-out time adjusted by "
						+ options.getCheckOutTimeCorrection() + " minutes...");
			}
			
			if (checkInTime.compareTo(checkOutTime) > 0)
				checkOutTime = checkOutTime.plusDays(1);
			
			// only accept passengers inside the time frame 06:00 - 20:00
//			if (!(	checkInTime.toLocalTime().compareTo(options.getCheckInLowerBound())		> 0 &&
//					checkOutTime.toLocalTime().compareTo(options.getCheckInLowerBound())	> 0 &&
//					checkOutTime.toLocalTime().compareTo(options.getCheckOutUpperBound())	< 0 &&
//					checkInTime.toLocalTime().compareTo(options.getCheckOutUpperBound())	< 0)) {
//				line = br.readLine();
//				continue;
//			}
			
			if (!(checkInTime.toLocalTime().compareTo(options.getCheckInLowerBound()) >= 0 &&
					checkOutTime.toLocalTime().compareTo(options.getCheckOutUpperBound()) <= 0)) {
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
			List<DefaultPath> paths = generator.generateRoutes(
					passenger.getFromStation().name(),
					passenger.getToStation().name(), 
					passenger.getCheckInTime(), 
					passenger.getCheckOutTime());
			RouteSelection selector = new SLTLARouteSelection(
					passenger.getCheckInTime(), 
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
	
	public void exportPassengers(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		log.info("Begin export of CiCo data ...");
		long count = 0;
		for (Passenger passenger : passengers) {
			bw.write(passenger.getCheckInTime().toString() + ",");
			bw.write(passenger.getCheckOutTime().toString() + ",");
			bw.write(passenger.getFromStation().name() + ",");
			bw.write(passenger.getToStation().name() + System.lineSeparator());
			count++;
			if (count % 1000 == 0)
				log.info("... Processed " + count + " passengers");
		}
		bw.flush();
		bw.close();
	}
	
	public static void drawPassengerArrivalRate(Collection<Passenger> passengers, int interval) {
		@SuppressWarnings("unchecked")
		DataTable table = new DataTable(Integer.class, Integer.class);
		List<Integer> frequencies = arrivalsToFrequencies(passengers, interval);
		for (int i = 0; i < frequencies.size(); i++) {
			table.add(i+1, frequencies.get(i));
		}
		FrequencyPlot plot = new FrequencyPlot(table);
		plot.setVisible(true);
	}
	
	private static List<Integer> arrivalsToFrequencies(Collection<Passenger> passengers, int interval) {
		List<Integer> frequencies = new ArrayList<>();
		Object[] sortedPassengers = passengers.toArray();
		Arrays.sort(sortedPassengers);
		LocalTime referenceDateTime = LocalTime.parse("00:00:00");
		int counter = 0;
		for (Object o : sortedPassengers) {
			Passenger passenger = (Passenger) o;
			if (referenceDateTime == null)
				referenceDateTime = passenger.getCheckInTime().toLocalTime();
			if (passenger.getCheckInTime().toLocalTime().compareTo(referenceDateTime.plusMinutes(interval)) >= 0) {
				while (passenger.getCheckInTime().toLocalTime().compareTo(referenceDateTime.plusMinutes(interval)) >= 0) {
					frequencies.add(counter);
					counter = 0;
					referenceDateTime = referenceDateTime.plusMinutes(interval);
				}
				counter++;
			} else {
				counter++;
			}
		}
		
		// add 0 padding to end of day
		int neededSize = (int)Math.ceil(1440f/interval);
		while (frequencies.size() < neededSize)
			frequencies.add(0);
		return frequencies;
	}
	
	public Collection<Passenger> getPassengersWithJourney(String fromStation, String toStation) {
		List<Passenger> selectedPassengers = new ArrayList<>();
		for (Passenger passenger : passengers) {
			if (passenger.getFromStation().name().equals(fromStation) &&
					passenger.getToStation().name().equals(toStation)) {
				selectedPassengers.add(passenger);
			}
		}
		return selectedPassengers;
	}
	
	public Collection<Passenger> getPassengersBetween(LocalTime time1, LocalTime time2) {
		List<Passenger> selectedPassengers = new ArrayList<>();
		for (Passenger passenger : passengers) {
			if (passenger.getCheckInTime().toLocalTime().compareTo(time1) >= 0 &&
					passenger.getCheckOutTime().toLocalTime().compareTo(time2) <= 0) {
				selectedPassengers.add(passenger);
			}
		}
		return selectedPassengers;
	}
	
	private static class FrequencyPlot extends JFrame {
		public FrequencyPlot(DataTable data) {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setSize(800, 600);
			
			XYPlot plot = new XYPlot(data);
			LineRenderer lines = new DefaultLineRenderer2D();
			plot.setLineRenderers(data, lines);
			Color color = new Color(0.0f, 0.3f, 1.0f);
			lines.setColor(color);
			getContentPane().add(new InteractivePanel(plot));
		}
	}
}
