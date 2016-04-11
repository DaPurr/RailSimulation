package wagon;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
//		EventActivityNetwork network = EventActivityNetwork.createTestNetwork();
		try {
			Timetable sample = Timetable.importFromExcel("data/smaller_sample_schedule.xlsx");
			EventActivityNetwork network = EventActivityNetwork.createNetwork(sample);
			System.out.println(network);
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}
