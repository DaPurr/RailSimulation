package wagon.timetable;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.monitorjbl.xlsx.StreamingReader;

import wagon.infrastructure.Station;
import wagon.rollingstock.Composition;
import wagon.rollingstock.TrainType;

/**
 * This class represents a train timetable, where we store the departures per station. 
 * It is designed mainly to construct timetable-loyal networks.
 * 
 * @author Nemanja Milovanovic
 * 
 */

public class Timetable {
	
	private Map<Station, List<ScheduledTrip>> departures;
	private Map<Composition,SortedSet<ScheduledTrip>> routes;
	private Set<Station> stations;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Create an empty <code>Timetable</code> object.
	 */
	public Timetable() {
		departures = new HashMap<>();
		routes = new HashMap<>();
		stations = new HashSet<>();
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
	}
	
	/**
	 * @return	<code>Set</code> of all compositions in the timetable
	 */
	public Set<Composition> compositions() {
		return new HashSet<>(routes.keySet());
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
	public static Timetable importFromExcel(String filename) 
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
		int rowCount = 1;
//		XSSFSheet sheet = workbook.getSheetAt(0);
//		Iterator<Row> rowIterator = sheet.rowIterator();
		for (Row row : reader) {
//			Row row = rowIterator.next();
			Cell cell = row.getCell(0);
			// skip row if we have a header line
			if (cell.getCellType() == Cell.CELL_TYPE_STRING)
				continue;
			
			// TODO: save timetable per day of the week
//			System.out.println(row.getCell(0).getStringCellValue());
//			System.out.println(row.getCell(1).getStringCellValue());
//			System.out.println(row.getCell(2).getStringCellValue());
//			System.out.println(row.getCell(3).getStringCellValue());
//			System.out.println(row.getCell(4).getStringCellValue());
//			System.out.println(row.getCell(5).getStringCellValue());
//			System.out.println(row.getCell(6).getStringCellValue());
//			System.out.println(row.getCell(7).getStringCellValue());
//			System.out.println(row.getCell(8).getStringCellValue());
			
			CellReference cellRef = new CellReference("L");
			
			int dayOfWeek = (int) row.getCell(cellRef.getCol()).getNumericCellValue();
			// only save mondays
			if (dayOfWeek != 1)
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
			cellRef = new CellReference("H");
			TrainType trainType = extractTrainTypeFromCell(row.getCell(cellRef.getCol()));
			
			Composition comp = new Composition(trainNr, trainType, nrWagons, 
					capSeats1, capSeats2 + capSeats2Fold + capStand2);
			ScheduledTrip trip = new ScheduledTrip(comp, departureTime, arrivalTime, 
					fromStation, toStation);
			timetable.addStation(fromStation, trip);
			timetable.stations.add(fromStation);
			timetable.stations.add(toStation);
			if (rowCount % 500 == 0)
				timetable.log.info("...Processed row " + rowCount);
			rowCount++;
		}
//		workbook.close();
		reader.close();
		timetable.log.info("...Finished importing Excel");
		
		// display some characteristics
		timetable.log.info("Number of stations (departure): " + timetable.departures.keySet().size());
		timetable.log.info("Number of stations: (dep & arr) " + timetable.departures.keySet().size());
		
		return timetable;
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
		String date = "2016-04-11T" + fourth + third + ":" + second + first + ":00";
		LocalDateTime datetime = LocalDateTime.parse(date);
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
}
