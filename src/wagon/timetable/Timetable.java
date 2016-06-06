package wagon.timetable;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.monitorjbl.xlsx.StreamingReader;

import wagon.infrastructure.Station;
import wagon.rollingstock.ComfortNorm;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;

/**
 * This class represents a train timetable, where we store the departures per station. 
 * It is designed mainly to construct timetable-loyal networks.
 * 
 * @author Nemanja Milovanovic
 * 
 */
public class Timetable implements Iterable<ScheduledTrip> {
	
	// reference date-time
	private static final LocalDateTime referenceDateTime = 
			LocalDateTime.of(2016, 04, 11, 0, 0);
	
	private Map<Station, List<ScheduledTrip>> departures;
	private Map<Composition, SortedSet<ScheduledTrip>> routes;
	private Set<Station> stations;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create an empty <code>Timetable</code> object.
	 */
	public Timetable() {
		departures = new LinkedHashMap<>();
		routes = new LinkedHashMap<>();
		stations = new LinkedHashSet<>();
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
	public void addStation(Station station, List<ScheduledTrip> trips) {
		if (station == null || trips == null)
			throw new IllegalArgumentException("Arguments can't be null");
		for (ScheduledTrip trip : trips) {
			addStation(station, trip);
		}
	}
	
	public Set<Station> getStations() {
		return new LinkedHashSet<>(stations);
	}
	
	/**
	 * Adds a single departure to the timetable, where the departure is mapped 
	 * to a station. The departures are then sorted. 
	 * 
	 * @param station		station of departure
	 * @param departure		<code>ScheduledDeparture</code> object representing 
	 * 						the actual departure
	 */
	public void addStation(Station station, ScheduledTrip trip) {
		if (station == null || trip == null)
			throw new IllegalArgumentException("Arguments can't be null");
		if (!departures.containsKey(station)) {
			List<ScheduledTrip> deps = new ArrayList<>();
			deps.add(trip);
			departures.put(station, deps);
		} else {
			List<ScheduledTrip> deps = departures.get(station);
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
	public Set<Composition> compositions() {
		return new LinkedHashSet<>(routes.keySet());
	}
	
	public List<ScheduledTrip> getRoute(Composition comp) {
		if (!routes.containsKey(comp))
			return null;
		return new ArrayList<>(routes.get(comp));
	}
	
	/**
	 * @param 	station	departure station
	 * @return	sorted list of departures from <code>station</code>
	 */
	public List<ScheduledTrip> departuresByStation(Station station) {
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
			List<ScheduledTrip> deps = departuresByStation(station);
			for (int i = 0; i < deps.size(); i++) {
				s += "\t";
				if (i > 0)
					s += "\t";
				ScheduledTrip dep = deps.get(i);
				s += dep.toStation().name() + " ";
				s += dep.departureTime() + " ";
				s += dep.composition().type().toString() + "_"
						+ dep.composition().getNrWagons() + "\n";
			}
		}
		s += "]";
		return s;
	}
	
	private void addTrip(ScheduledTrip trip) {
		Composition comp = trip.composition();
		if (!routes.containsKey(comp)) {
			SortedSet<ScheduledTrip> set = new TreeSet<>();
			set.add(trip);
			routes.put(comp, set);
		} else {
			SortedSet<ScheduledTrip> set = routes.get(comp);
			set.add(trip);
		}
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
	public static Timetable importFromExcel(String filename, int day) 
			throws InvalidFormatException, IOException {
		// only allow xls(x) files
		if (!filename.matches(".*\\.xls.?"))
			throw new IllegalArgumentException("File needs to be excel format.");
		Timetable timetable = new Timetable();
		timetable.log.info("File: " + filename + " is xls(x).");
		File file = new File(filename);
//		InputStream is = new FileInputStream(file);
		
		// code from Stack Overflow to stream input
		StreamingReader reader = StreamingReader.builder()
				.rowCacheSize(100)
				.bufferSize(1024)
				.sheetIndex(0)
				.read(file);
		
		timetable.log.info("Begin parsing Excel...");
//		XSSFWorkbook workbook = new XSSFWorkbook(file);
		timetable.log.info("...Finished parsing Excel");
		timetable.log.info("Begin Excel import...");
		
		Map<Composition, List<ScheduledTrip>> trainRoutes = 
				new LinkedHashMap<>();
		
		int rowCount = 1;
		for (Row row : reader) {
			Cell cell = row.getCell(0);
			// skip row if we have a header line
			if (cell.getCellType() == Cell.CELL_TYPE_STRING)
				continue;
			
			// TODO: save timetable per day of the week
			
			CellReference cellRef = new CellReference("L");
			
			int dayOfWeek = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			// only save mondays
			if (dayOfWeek != day)
				continue;
			
			// init variables to store timetable
			cellRef = new CellReference("A");
			LocalDateTime departureTime = extractDateFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("BS");
			LocalDateTime arrivalTime = extractDateFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("B");
			int trainNr = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("C");
			Station fromStation = extractStationFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("D");
			Station toStation = extractStationFromCell(row.getCell(cellRef.getCol()));
			cellRef = new CellReference("AU");
			int nrWagons = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BF");
			int capSeats1 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BG");
			int capSeats2 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BH");
			int capSeats2Fold = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BI");
			int capStand2 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("AY");
			int capNormC1 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("AZ");
			int capNormC2 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BA");
			int capNormA2 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("BB");
			int capNormV2 = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			cellRef = new CellReference("G");
			ComfortNorm norm = ComfortNorm.valueOf(row.getCell(cellRef.getCol()).getStringCellValue());
			cellRef = new CellReference("H");
			TrainType trainType = extractTrainTypeFromCell(row.getCell(cellRef.getCol()));
			
			Composition comp = new Composition(trainNr, trainType, nrWagons, 
					capSeats1, capSeats2, capSeats2Fold, capStand2, capNormC1, 
					capNormC2, capNormA2, capNormV2, norm);
			ScheduledTrip trip = new ScheduledTrip(comp, departureTime, arrivalTime, 
					fromStation, toStation);
			List<ScheduledTrip> trips = trainRoutes.get(comp);
			if (trips == null) {
				trips = new ArrayList<>();
				trainRoutes.put(comp, trips);
			}
			trips.add(trip);
//			timetable.addStation(fromStation, trip);
//			timetable.stations.add(fromStation);
//			timetable.stations.add(toStation);
			if (rowCount % 500 == 0)
				timetable.log.info("...Processed row " + rowCount);
			rowCount++;
		}
//		workbook.close();
		reader.close();
		
		// fixing departure/arrival times
		fixTripTimes(trainRoutes);
		for (Entry<Composition, List<ScheduledTrip>> entry : trainRoutes.entrySet()) {
			for (ScheduledTrip trip : entry.getValue()) {
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
	
	private static void fixTripTimes(Map<Composition, List<ScheduledTrip>> trainRoutes) {
		for (Entry<Composition, List<ScheduledTrip>> entry : trainRoutes.entrySet()) {
			List<ScheduledTrip> trips = entry.getValue();
			
			LocalDateTime previousDateTime = null;
			for (ScheduledTrip trip : trips) {
				LocalDateTime departureDateTime = trip.departureTime();
				
				// case 1: departure before 00:00, arrival after 00:00
				if (trip.departureTime().compareTo(trip.arrivalTime()) > 0)
					trip.setArrivalTime(trip.arrivalTime().plusDays(1));
				
				if (previousDateTime == null) {
					previousDateTime = departureDateTime;
					continue;
				}
				
				// case 2: both departure and arrival after 00:00
				if (departureDateTime.compareTo(previousDateTime) < 0) {
					departureDateTime = trip.departureTime().plusDays(1);
					trip.setDepartureTime(departureDateTime);
					trip.setArrivalTime(trip.arrivalTime().plusDays(1));
				}
				previousDateTime = departureDateTime;
			}
		}
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
			String type = composition.getAttribute("type");
			String nrUnits = composition.getAttribute("nrUnits");
			String seats1 = composition.getAttribute("seats1");
			String seats2 = composition.getAttribute("seats2");
			String foldable = composition.getAttribute("foldable");
			String standArea = composition.getAttribute("standArea");
			String normC1 = composition.getAttribute("normC1");
			String normC2 = composition.getAttribute("normC2");
			String normA2 = composition.getAttribute("normA2");
			String normV2 = composition.getAttribute("normV2");
			String norm = composition.getAttribute("norm");
			Composition comp = new Composition(Integer.parseInt(id), 
					TrainType.valueOf(type), 
					Integer.valueOf(nrUnits),
					Integer.valueOf(seats1),
					Integer.valueOf(seats2),
					Integer.valueOf(foldable),
					Integer.valueOf(standArea),
					Integer.valueOf(normC1),
					Integer.valueOf(normC2),
					Integer.valueOf(normA2),
					Integer.valueOf(normV2),
					ComfortNorm.valueOf(norm));
			
			String departureDate = trip.getAttribute("departureTime");
			String arrivalDate = trip.getAttribute("arrivalTime");
			String from = trip.getAttribute("from");
			String to = trip.getAttribute("to");
			Station fromStation = new Station(from);
			ScheduledTrip st = new ScheduledTrip(comp, 
					LocalDateTime.parse(departureDate), 
					LocalDateTime.parse(arrivalDate), 
					fromStation, new Station(to));
			
			timetable.addStation(fromStation, st);
			tripCount++;
			if (tripCount % 5000 == 0)
				timetable.log.info("...Parsed " + tripCount + " trips");
		}
		
		timetable.log.info("...Finished parsing trips");
		timetable.log.info("...Finished import of XML timetable");
		
		return timetable;
	}
	
	public void export(String file_name) throws IOException {
		File file = new File(file_name);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("<timetable>");
		bw.newLine();
		for (SortedSet<ScheduledTrip> set : routes.values()) {
			for (ScheduledTrip trip : set) {
				bw.write(tripToXML(trip, 1));
				bw.newLine();
			}
		}
		bw.write("</timetable>");
		bw.flush();
		bw.close();
	}
	
	private String tripToXML(ScheduledTrip trip, int indentLevel) {
		String s = indent(indentLevel) + "<trip ";
		s += "from=\"" + trip.fromStation().name() + "\" ";
		s += "to=\"" + trip.toStation().name() + "\" ";
		s += "departureTime=\"" + trip.departureTime().toString() + "\" ";
		s += "arrivalTime=\"" + trip.arrivalTime().toString() + "\">" + System.lineSeparator();
		s += indent(indentLevel + 1) + "<composition ";
		Composition comp = trip.composition();
		s += "id=\"" + comp.id() + "\" ";
		s += "type=\"" + comp.type().toString() + "\" ";
		s += "nrUnits=\"" + comp.getNrWagons() + "\" ";
		s += "seats1=\"" + comp.getSeats1() + "\" ";
		s += "seats2=\"" + comp.getSeats2() + "\" ";
		s += "foldable=\"" + comp.getFoldableSeats2() + "\" ";
		s += "standArea=\"" + comp.getStandArea2() + "\" ";
		s += "normC1=\"" + comp.getNormC1() + "\" ";
		s += "normC2=\"" + comp.getNormC2() + "\" ";
		s += "normA2=\"" + comp.getNormA2() + "\" ";
		s += "normV2=\"" + comp.getNormV2() + "\" ";
		s += "norm=\"" + comp.getNorm() + "\" /> " + System.lineSeparator();
		s += indent(indentLevel) + "</trip>";
		return s;
	}
	
	private String indent(int n) {
		String s = "";
		for (int i = 0; i < n; i++)
			s += "   ";
		return s;
	}
	
	private static LocalDateTime extractDateFromCell(Cell cell) {
		if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC)
			throw new IllegalArgumentException("Wrong cell type for dates.");
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
//		String date = "2016-04-11T" + fourth + third + ":" + second + first + ":00";
		LocalDateTime datetime = referenceDateTime
				.withHour(fourth*10 + third)
				.withMinute(second*10 + first);
		return datetime;
	}
	
	private static Station extractStationFromCell(Cell cell) {
		if (cell.getCellType() != Cell.CELL_TYPE_STRING)
			throw new IllegalArgumentException("Wrong cell type for stations.");
		return new Station(cell.getStringCellValue());
	}
	
	private static TrainType extractTrainTypeFromCell(Cell cell) {
		if (cell.getCellType() != Cell.CELL_TYPE_STRING)
			throw new IllegalArgumentException("Wrong cell type for train types.");
		return TrainType.valueOf(cell.getStringCellValue());
	}

	@Override
	public Iterator<ScheduledTrip> iterator() {
		Set<ScheduledTrip> newSet = new LinkedHashSet<>();
		for (Set<ScheduledTrip> set : routes.values()) {
			newSet.addAll(set);
		}
		return newSet.iterator();
	}
	
	/**
	 * @return	returns all trips associated to the timetable
	 */
	public Set<ScheduledTrip> getAllTrips() {
		Set<ScheduledTrip> trips = new LinkedHashSet<>();
		for (Entry<Composition, SortedSet<ScheduledTrip>> entry : routes.entrySet()) {
			Set<ScheduledTrip> set = entry.getValue();
			trips.addAll(set);
		}
		return trips;
	}
}
