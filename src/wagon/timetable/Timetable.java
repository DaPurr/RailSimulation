package wagon.timetable;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.google.common.collect.*;
import com.monitorjbl.xlsx.StreamingReader;

import wagon.infrastructure.Station;
import wagon.rollingstock.*;

/**
 * This class represents a train timetable, where we store the departures per station. 
 * It is designed mainly to construct timetable-loyal networks.
 * 
 * @author Nemanja Milovanovic
 * 
 */
public class Timetable {
	
	private Map<Station, List<Trip>> departures;
	private Map<Integer, SortedSet<Trip>> routes;
	private Set<Station> stations;
	private Set<TrainService> trainServices; 
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create an empty <code>Timetable</code> object.
	 */
	public Timetable() {
		departures = new LinkedHashMap<>();
		routes = new LinkedHashMap<>();
		stations = new LinkedHashSet<>();
		log.setLevel(Level.ALL);
		trainServices = new LinkedHashSet<>();
	}
	
	public Timetable(Timetable timetable) {
		Map<Station, List<Trip>> newDepartures = 
				new LinkedHashMap<>();
		Map<Integer, SortedSet<Trip>> newRoutes = 
				new LinkedHashMap<>();
		Set<Station> newStations = new LinkedHashSet<>();
		Set<TrainService> newTrainServices = new LinkedHashSet<>();
		for (Entry<Integer, SortedSet<Trip>> entry : timetable.routes.entrySet()) {
			SortedSet<Trip> sortedTrips = entry.getValue();
			SortedSet<Trip> newSortedTrips = new TreeSet<>();
			for (Trip trip : sortedTrips) {
				Trip tripCopy = trip.copy();
				
				newSortedTrips.add(tripCopy);
				
				newStations.add(tripCopy.fromStation());
				newStations.add(tripCopy.toStation());
				
				newTrainServices.add(tripCopy.getTrainService());
				
				List<Trip> departuresList = newDepartures.get(tripCopy.fromStation());
				if (departuresList == null) {
					departuresList = new ArrayList<>();
					newDepartures.put(tripCopy.fromStation(), departuresList);
				}
				departuresList.add(tripCopy);
			}
			newRoutes.put(entry.getKey(), newSortedTrips);
		}
		
		departures = newDepartures;
		routes = newRoutes;
		stations = newStations;
		trainServices = newTrainServices;
		
		log.setLevel(Level.ALL);
	}
	
	/**
	 * This method maps a station to a list of departures, where the departures are 
	 * meant to be used as reference. Note: the departures are sorted in ascending 
	 * order according to departure time.
	 * 
	 * @param station	origin station
	 * @param dep		<code>ScheduledDeparture</code> object giving time of 
	 * 					departure, and destination station.
	 */
	public void addStation(Station station, List<Trip> trips) {
		if (station == null || trips == null)
			throw new IllegalArgumentException("Arguments can't be null");
		for (Trip trip : trips) {
			addStation(station, trip);
		}
	}
	
	public Set<Station> getStations() {
		return new LinkedHashSet<>(departures.keySet());
	}
	
	/**
	 * Adds a single departure to the timetable, where the departure is mapped 
	 * to a station. The departures are then sorted. 
	 * 
	 * @param station		station of departure
	 * @param departure		<code>ScheduledDeparture</code> object representing 
	 * 						the actual departure
	 */
	public void addStation(Station station, Trip trip) {
		if (station == null || trip == null)
			throw new IllegalArgumentException("Arguments can't be null");
		if (!departures.containsKey(station)) {
			List<Trip> deps = new ArrayList<>();
			deps.add(trip);
			departures.put(station, deps);
		} else {
			List<Trip> deps = departures.get(station);
			deps.add(trip);
			Collections.sort(deps);
		}
		
		// add trip to composition route
		addTrip(trip);
		stations.add(station);
	}
	
	/**
	 * @return	<code>Set</code> of all compositions in the timetable
	 */
	public Set<TrainService> getTrainServices() {
		return new LinkedHashSet<>(trainServices);
	}
	
	public SortedSet<Trip> getRoute(TrainService comp) {
		Set<Trip> route = routes.get(comp.id());
		if (route == null)
			return null;
		return new TreeSet<>(route);
	}
	
	public SortedSet<Trip> getRoute(TrainService comp, int dayOfWeek) {
		SortedSet<Trip> route = routes.get(comp.id());
		if (route == null)
			return null;
		SortedSet<Trip> trips = new TreeSet<>();
		for (Trip trip : route) {
			if (trip.getDayOfWeek() == dayOfWeek)
				trips.add(trip);
		}
		return trips;
	}
	
	/**
	 * @param 	station	departure station
	 * @return	sorted list of departures from <code>station</code>
	 */
	public List<Trip> departuresByStation(Station station) {
		if (station == null || !departures.containsKey(station))
			throw new IllegalArgumentException("Station not available: " + station);
		return new ArrayList<>(departures.get(station));
	}
	
	/**
	 * @return	number of stations
	 */
	public int size() {
		return departures.size();
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Timetable))
			return false;
		Timetable o = (Timetable) other;
		return this.departures.equals(o.departures) && this.routes.equals(o.routes);
	}
	
	@Override
	public int hashCode() {
		return 5*departures.hashCode() + 7*routes.hashCode();
	}
	
	@Override
	public String toString() {
		String s = "[\n";
		for (Station station : departures.keySet()) {
			s += "  ";
			s += station.name();
			if (departures.get(station) == null)
				continue;
			List<Trip> deps = departuresByStation(station);
			for (int i = 0; i < deps.size(); i++) {
				s += "\t";
				if (i > 0)
					s += "\t";
				Trip dep = deps.get(i);
				s += dep.toStation().name() + " ";
				s += dep.departureTime() + " ";
				s += dep.getTrainService().type().toString() + "_"
						+ dep.getTrainService().getNrWagons() + System.lineSeparator();
			}
		}
		s += "]";
		return s;
	}
	
	private void addTrip(Trip trip) {
		TrainService comp = trip.getTrainService();
		SortedSet<Trip> set = routes.get(comp.id());
		if (set == null) {
			set = new TreeSet<>();
			routes.put(comp.id(), set);
		}
		set.add(trip);
		trainServices.add(comp);
	}
	
	/**
	 * Imports a timetable expressed in an xls(x) file. The file needs to have the same format as
	 * ComfortNormeringTool_v0.3. If this format is not adhered, undefined behavior will follow.
	 * 
	 * @param filename	name of the file containing the timetable
	 * @return	<code>Timetable</code> object obtained from <code>filename</code>
	 * 
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	public static Timetable importFromExcel(String filename, boolean hasHeader) 
			throws InvalidFormatException, IOException {
		// only allow xls(x) files
		if (!filename.matches(".*\\.xls.?"))
			throw new IllegalArgumentException("File needs to be excel format.");
		Timetable timetable = new Timetable();
		timetable.log.info("File: " + filename + " is xls(x).");
//		File file = new File(filename);
//		InputStream is = new FileInputStream(file);
		
		// code from Stack Overflow to stream input
		InputStream is = new FileInputStream(new File(filename));
		Workbook workbook = StreamingReader.builder()
				.rowCacheSize(100)
				.bufferSize(1024)
				.open(is);
		
		timetable.log.info("Begin parsing Excel...");
//		XSSFWorkbook workbook = new XSSFWorkbook(file);
		timetable.log.info("...Finished parsing Excel");
		timetable.log.info("Begin Excel import...");
		
		Map<TrainService, List<Trip>> trainRoutes = 
				new LinkedHashMap<>();
		
		int rowCount = 1;
		Sheet sheet = workbook.getSheetAt(0);
		for (Row row : sheet) {
			// skip row if we have a header line
			if (hasHeader) {
				hasHeader = false;
				continue;
			}
			
			CellReference cellRef = new CellReference("L");
			
			int dayOfWeek = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			
//			if (dayOfWeek != day)
//				continue;
			
			// init variables to store timetable
			cellRef = new CellReference("A");
			LocalTime departureTime = extractTimeFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("BS");
			LocalTime arrivalTime = extractTimeFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("B");
			int trainNr = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("C");
			Station fromStation = extractStationFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("D");
			Station toStation = extractStationFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("G");
			ComfortNorm norm = ComfortNorm.valueOf(row.getCell(cellRef.getCol()).getStringCellValue());
			cellRef = new CellReference("AV");
			String combination = row.getCell(cellRef.getCol()).getStringCellValue();
			
			TrainService comp = timetable.parseComposition(trainNr, combination);
			
			Trip trip = new Trip(comp, departureTime, arrivalTime, 
					fromStation, toStation, norm, dayOfWeek);
			List<Trip> trips = trainRoutes.get(comp);
			if (trips == null) {
				trips = new ArrayList<>();
				trainRoutes.put(comp, trips);
			}
			trips.add(trip);
			if (rowCount % 500 == 0)
				timetable.log.info("...Processed row " + rowCount);
			rowCount++;
		}
//		workbook.close();
		workbook.close();
		
		// fixing departure/arrival times
		trainRoutes = fixTripTimes(trainRoutes);
		for (Entry<TrainService, List<Trip>> entry : trainRoutes.entrySet()) {
			for (Trip trip : entry.getValue()) {
				Station fromStation = trip.fromStation();
				Station toStation = trip.toStation();
				timetable.addStation(fromStation, trip);
				timetable.stations.add(fromStation);
				timetable.stations.add(toStation);
			}
		}
		
		timetable.log.info("...Finished importing Excel");
		
		// display some characteristics
		timetable.log.info("Number of stations (departure): " + timetable.departures.keySet().size());
		timetable.log.info("Number of stations: (dep & arr) " + timetable.departures.keySet().size());
		
		return timetable;
	}
	
	private TrainService parseComposition(int id, String combination) {
		String[] parts = combination.split("-");
		Multiset<RollingStockUnit> units = LinkedHashMultiset.create();
		for (String part : parts) {
			
			if (part.equals("LK"))
				units.add(new DDM4Unit());
			
			else if (part.equals("LBM"))
				units.add(new DDZ4Unit());
			else if (part.equals("LAM"))
				units.add(new DDZ6Unit());
			
			else if (part.equals("ZN"))
				units.add(new DM902Unit());
			
			else if (part.equals("OH"))
				units.add(new ICM3Unit());
			else if (part.equals("OC"))
				units.add(new ICM4Unit());
			
			else if (part.equals("LV"))
				units.add(new SGM2Unit());
			else if (part.equals("LM"))
				units.add(new SGM3Unit());
			
			else if (part.equals("LE"))
				units.add(new SLT4Unit());
			else if (part.equals("LC"))
				units.add(new SLT6Unit());
			
			else if (part.equals("AD"))
				units.add(new VIRM4Unit());
			else if (part.equals("OA"))
				units.add(new VIRM6Unit());
		}
		
		Composition comp = new Composition(units);
		TrainService composition = new TrainService(id, comp);
		
		return composition;
	}
	
	private static Map<TrainService, List<Trip>> fixTripTimes(
			Map<TrainService, List<Trip>> trainRoutes) {
		Map<TrainService, List<Trip>> newRoutes = new HashMap<>();
		for (Entry<TrainService, List<Trip>> entry : trainRoutes.entrySet()) {
			List<Trip> trips = entry.getValue();
			List<Trip> newTrips = new ArrayList<>();
			
			for (Trip trip : trips) {
				if (trip.departureTime().compareTo(trip.arrivalTime()) < 0)
					newTrips.add(trip);
				else break;
			}
			newRoutes.put(entry.getKey(), newTrips);
		}
		return newRoutes;
	}
	
	public static Timetable importFromXML(String file_name) throws SAXException, IOException, ParserConfigurationException {
		if (!file_name.matches(".*\\.xml"))
			throw new IllegalArgumentException("File needs to be XML format.");
		Timetable timetable = new Timetable();
		
		timetable.log.info("Begin import XML timetable...");
		
		// initialize
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder(); 
		Document doc = db.parse(new File(file_name));
		
		// parse the document
		NodeList trips = doc.getElementsByTagName("trip");
		int nrTrips = trips.getLength();
		int tripCount = 0;
		timetable.log.info("Begin parsing trips...");
		for (int i = 0; i < nrTrips; i++) {
			
			// parse trip
			Element trip = (Element) trips.item(i);
			
			Element composition = (Element) trip.getElementsByTagName("composition").item(0);
			String id = composition.getAttribute("id");
			
			TrainService comp = new TrainService(
					Integer.parseInt(id), 
					timetable.parseUnitsToComposition(composition));
			
			String departureDate = trip.getAttribute("departureTime");
			String arrivalDate = trip.getAttribute("arrivalTime");
			String from = trip.getAttribute("from");
			String to = trip.getAttribute("to");
			Station fromStation = new Station(from);
			ComfortNorm norm = ComfortNorm.valueOf(trip.getAttribute("norm"));
			int dayOfWeek = Integer.parseInt(trip.getAttribute("day"));
			Trip st = new Trip(comp, 
					LocalTime.parse(departureDate), 
					LocalTime.parse(arrivalDate), 
					fromStation, new Station(to), 
					norm, 
					dayOfWeek);
			
			timetable.addStation(fromStation, st);
			tripCount++;
			if (tripCount % 5000 == 0)
				timetable.log.info("...Parsed " + tripCount + " trips");
		}
		
		timetable.log.info("...Finished parsing trips");
		timetable.log.info("...Finished import of XML timetable");
		
		return timetable;
	}
	
	private Composition parseUnitsToComposition(Element e) {
		Multiset<RollingStockUnit> units = LinkedHashMultiset.create();
		String[] parts = e.getAttribute("units").split("-");
		
		for (String part : parts) {
			if (part.equals("DM902"))
				units.add(new DM902Unit());
			
			else if (part.equals("DDM4"))
				units.add(new DDM4Unit());
			
			else if (part.equals("DDZ4"))
				units.add(new DDZ4Unit());
			else if (part.equals("DDZ6"))
				units.add(new DDZ6Unit());
			
			else if (part.equals("ICM3"))
				units.add(new ICM3Unit());
			else if (part.equals("ICM4"))
				units.add(new ICM4Unit());
			
			else if (part.equals("SGM2"))
				units.add(new SGM2Unit());
			else if (part.equals("SGM3"))
				units.add(new SGM3Unit());
			
			else if (part.equals("SLT4"))
				units.add(new SLT4Unit());
			else if (part.equals("SLT6"))
				units.add(new SLT6Unit());
			
			else if (part.equals("VIRM4"))
				units.add(new VIRM4Unit());
			else if (part.equals("VIRM6"))
				units.add(new VIRM6Unit());
		}
		return new Composition(units);
	}
	
	public void export(String file_name) throws IOException {
		File file = new File(file_name);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("<timetable>");
		bw.newLine();
		for (SortedSet<Trip> set : routes.values()) {
			for (Trip trip : set) {
				bw.write(tripToXML(trip, 1));
				bw.newLine();
			}
		}
		bw.write("</timetable>");
		bw.flush();
		bw.close();
	}
	
	private String tripToXML(Trip trip, int indentLevel) {
		String s = indent(indentLevel) + "<trip ";
		s += "from=\"" + trip.fromStation().name() + "\" ";
		s += "to=\"" + trip.toStation().name() + "\" ";
		s += "departureTime=\"" + trip.departureTime().toString() + "\" ";
		s += "arrivalTime=\"" + trip.arrivalTime().toString() + "\" ";
		s += "day=\"" + trip.getDayOfWeek() + "\" ";
		s += "norm=\"" + trip.getNorm().toString() + "\">";
		s += System.lineSeparator();
		
		TrainService comp = trip.getTrainService();
		s += compositionToXML(comp, indentLevel + 1);
		s += System.lineSeparator();
		
		s += indent(indentLevel) + "</trip>";
		return s;
	}
	
	private String compositionToXML(TrainService comp, int indentLevel) {
		String s = indent(indentLevel) + "<composition ";
		s += "id=\"" + comp.id() + "\" ";
		s += "units=\"" + compositionToString(comp) + "\" ";
		s += "/>";
		
		return s;
	}
	
	private String compositionToString(TrainService service) {
		String s = "";
		boolean first = true;
		for (RollingStockUnit unit : service.getComposition()) {
			if (first)
				first = false;
			else
				s += "-";
			s += unit.toString();
		}
		return s;
	}
	
	private String indent(int n) {
		String s = "";
		for (int i = 0; i < n; i++)
			s += "   ";
		return s;
	}
	
	private static LocalTime extractTimeFromCell(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			// hopefully we have been supplied a valid timestamp
			int number = (int) cell.getNumericCellValue();

			// now we do magic
			int fourth = number/1000;
			number -= fourth*1000;
			int third = number/100;
			number -= third*100;
			int second = number/10;
			number -= second*10;
			int first = number;
			LocalTime time = LocalTime.of(
					fourth*10 + third,
					second*10 + first);
			return time;
		} else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			DateTimeFormatter format = DateTimeFormatter.ofPattern("HHmm", Locale.US);
			LocalTime localTime = LocalTime.parse(cell.getStringCellValue(), format);
			return localTime;
		}
		throw new IllegalArgumentException("Wrong cell type for dates.");
	}
	
	private static Station extractStationFromCell(Cell cell) {
		if (cell.getCellType() != Cell.CELL_TYPE_STRING)
			throw new IllegalArgumentException("Wrong cell type for stations.");
		return new Station(cell.getStringCellValue());
	}
	
	/**
	 * @return	returns all trips associated to the timetable
	 */
	public Set<Trip> getAllTrips() {
		Set<Trip> trips = new LinkedHashSet<>();
		for (Entry<Integer, SortedSet<Trip>> entry : routes.entrySet()) {
			Set<Trip> set = entry.getValue();
			trips.addAll(set);
		}
		return trips;
	}
	
	public Set<Trip> getAllTrips(int dayOfWeek) {
		Set<Trip> trips = new LinkedHashSet<>();
		for (Entry<Integer, SortedSet<Trip>> entry : routes.entrySet()) {
			Set<Trip> set = entry.getValue();
			for (Trip trip : set) {
				if (trip.getDayOfWeek() == dayOfWeek)
					trips.add(trip);
			}
		}
		return trips;
	}
	
	public void cancelTrainsWithProbability(double psi) {
		
	}
	
	public void cancelTrain(Set<Integer> ids) {
		// remove train services from set of services
		for (Integer id : ids) {
			TrainService service = new TrainService(id, new Composition());
			trainServices.remove(service);
		}
		
		// now remove all associated trips
		for (Integer id : ids)
			routes.remove(id);
		for (Station station : departures.keySet()) {
			Iterator<Trip> tripIter = departures.get(station).iterator();
			while (tripIter.hasNext()) {
				Trip trip = tripIter.next();
				if (ids.contains(trip.getTrainService().id()))
					tripIter.remove();
			}
		}
	}
}
