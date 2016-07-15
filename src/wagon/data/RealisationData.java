package wagon.data;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import wagon.infrastructure.Station;
import wagon.rollingstock.*;

public class RealisationData {

	private Map<Integer, SortedSet<RealisationDataEntry>> entriesPerTrain;
	
	public RealisationData() {
		entriesPerTrain = new HashMap<>();
	}
	
	public static RealisationData importFromFile(String fileName) throws IOException {
		if (!fileName.matches(".*\\.csv"))
			throw new IllegalArgumentException("File needs to be excel format.");
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		RealisationData rdata = new RealisationData();
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			String[] parts = line.split(",");
			int trainNr = Integer.parseInt(parts[1]);
			LocalDateTime realizedDepartureTime = rdata.toLocalDateTimeObject(parts[3]);
			LocalDateTime plannedDepartureTime = rdata.toLocalDateTimeObject(parts[4]);
			Station departureStation = new Station(parts[5]);
			LocalDateTime realizedArrivalTime = rdata.toLocalDateTimeObject(parts[6]);
			LocalDateTime plannedArrivalTime = rdata.toLocalDateTimeObject(parts[7]);
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
		}
		
		br.close();
		return rdata;
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
	
	private LocalDateTime toLocalDateTimeObject(String text) {
		LocalDateTime date = LocalDateTime.parse(
				text.toLowerCase(), 
				DateTimeFormatter.ofPattern("ddMMMyyyy:HHmm:ss"));
		return date;
	}
	
	private Composition toComposition(int trainNr, String text) {
		if (text == null)
			throw new NullPointerException("Argument cannot be null");
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
		return RollingStockUnit.toUnit(TrainType.valueOf(parts[0]), Integer.parseInt(parts[1]));
	}
}
