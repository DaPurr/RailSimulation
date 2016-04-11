package wagon;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
//		EventActivityNetwork network = EventActivityNetwork.createTestNetwork();
		try {
			Timetable sample = Timetable.importFromExcel("data/sample_schedule.xlsx");
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}
