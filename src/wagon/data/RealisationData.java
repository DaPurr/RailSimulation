package wagon.data;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

import wagon.infrastructure.Station;
import wagon.rollingstock.*;

public class RealisationData {

	private Map<Integer, SortedSet<RealisationDataEntry>> entriesPerTrain;
	private final static LocalDateTime DUMMY = LocalDateTime.of(1992, 12, 05, 12, 0, 0);
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	public RealisationData() {
		entriesPerTrain = new HashMap<>();
	}
	
	public static RealisationData importFromFile(String fileName, String fileTrainNumbers) throws IOException {
		
		// create a set of train numbers from which we want realisation data
		Set<Integer> setTrainNumbers = importTrainNumbers(fileTrainNumbers);
		
		if (!fileName.matches(".*\\.csv"))
			throw new IllegalArgumentException("File needs to be excel format.");
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		RealisationData rdata = new RealisationData();
		br.readLine(); // throw away header
		long count = 0;
		rdata.log.info("Begin processing realisation data...");
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			line = fixLine(line);
			String[] parts = line.split(";");
			int trainNr = Integer.parseInt(parts[1]);
			
			if (setTrainNumbers.contains(trainNr)) {
				count++;
				LocalDateTime realizedDepartureTime = RealisationData.DUMMY;
				if (!parts[3].equals(""))
					realizedDepartureTime = rdata.toLocalDateTimeObjectRealized(parts[3]);
				LocalDateTime plannedDepartureTime = RealisationData.DUMMY;
				if (!parts[4].equals(""))
					plannedDepartureTime = rdata.toLocalDateTimeObjectPlanned(parts[4]);
				Station departureStation = new Station(parts[5]);
				LocalDateTime realizedArrivalTime = RealisationData.DUMMY;
				if (!parts[6].equals(""))
					realizedArrivalTime = rdata.toLocalDateTimeObjectRealized(parts[6]);
				LocalDateTime plannedArrivalTime = RealisationData.DUMMY;
				if (!parts[7].equals(""))
					plannedArrivalTime = rdata.toLocalDateTimeObjectPlanned(parts[7]);
				Station arrivalStation = new Station(parts[8]);
				Composition plannedComposition = rdata.toComposition(trainNr, parts[9]);
				Composition realizedComposition = rdata.toComposition(trainNr, parts[12]);

				RealisationDataEntry entry = rdata.addEntry(
						trainNr, 
						realizedDepartureTime, 
						plannedDepartureTime, 
						departureStation, 
						realizedArrivalTime, 
						plannedArrivalTime, 
						arrivalStation, 
						plannedComposition, 
						realizedComposition);
				if (count % 1000 == 0)
					rdata.log.info("Processed " + count + " entries...");
			}
		}
		
		br.close();
		rdata.log.info("... Finish processing realisation data");
		return rdata;
	}
	
	private static String fixLine(String line) {
		String newLine = "";
		String delimiter = ";";
		for (int i = 0; i < line.length(); i++) {
			String c = line.substring(i,i+1);
			if (c.equals(","))
				newLine = newLine.concat(delimiter);
			else if (c.equals("\"")) {
				if (delimiter.equals(","))
					delimiter = ";";
				else
					delimiter = ",";
			} else {
				newLine = newLine.concat(c);
			}
		}
		return newLine;
	}
	
	private static Set<Integer> importTrainNumbers(String fileName) throws IOException {
		Set<Integer> trainNumbers = new HashSet<>();
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			int trainNr = Integer.parseInt(line);
			trainNumbers.add(trainNr);
		}
		br.close();
		return trainNumbers;
	}
	
	private RealisationDataEntry addEntry(
			int trainNr, 
			LocalDateTime realizedDepartureTime, 
			LocalDateTime plannedDepartureTime, 
			Station departureStation, 
			LocalDateTime realizedArrivalTime, 
			LocalDateTime plannedArrivalTime, 
			Station arrivalStation, 
			Composition plannedComposition, 
			Composition realizedComposition) {
		SortedSet<RealisationDataEntry> set = entriesPerTrain.get(trainNr);
		if (set == null) {
			set = new TreeSet<>();
			entriesPerTrain.put(trainNr, set);
		}
		RealisationDataEntry entry = new RealisationDataEntry(
				trainNr, 
				realizedDepartureTime, 
				plannedDepartureTime, 
				departureStation, 
				realizedArrivalTime, 
				plannedArrivalTime, 
				arrivalStation, 
				plannedComposition, 
				realizedComposition);
		set.add(entry);
		return entry;
	}
	
	private LocalDateTime toLocalDateTimeObjectRealized(String text) {
		LocalDateTime date = LocalDateTime.parse(
				text.toLowerCase(), 
				DateTimeFormatter.ofPattern("ddMMMyyyy:HH:mm:ss"));
		return date;
	}
	
	private LocalDateTime toLocalDateTimeObjectPlanned(String text) {
		LocalDateTime date = LocalDateTime.parse(
				text.toLowerCase(), 
				DateTimeFormatter.ofPattern("ddMMMyy:HH:mm:ss"));
		return date;
	}
	
	private Composition toComposition(int trainNr, String text) {
		if (text == null)
			throw new NullPointerException("Argument cannot be null");
		else if (text.equals(""))
			return new Composition(trainNr, new ArrayList<>());
		text = text.replaceAll("[^ ,a-zA-Z0-9]", "");
		String[] parts = text.split(",");
		List<RollingStockUnit> units = new ArrayList<>();
		for (int i = 0; i < parts.length; i++) {
			RollingStockUnit unit = toUnit(parts[i]);
			units.add(unit);
		}
		return new Composition(trainNr, units);
	}
	
	private RollingStockUnit toUnit(String text) {
		if (text == null)
			throw new NullPointerException("Argument cannot be null");
		String[] parts = text.split(" ");
		String type = parts[0];
		int nrWagons = Integer.parseInt(parts[1]);
		if (type.equals("SGMM") || type.equals("SGMW"))
			type = "SGM";
		else if (type.equals("DDAR") && nrWagons == 3)
			type= "DDM";
		return RollingStockUnit.toUnit(TrainType.valueOf(type), nrWagons);
	}
}
