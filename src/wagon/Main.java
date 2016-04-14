package wagon;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import wagon.algorithms.DijkstraShortestPath;
import wagon.algorithms.Path;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
//		EventActivityNetwork network = EventActivityNetwork.createTestNetwork();
		try {
			Timetable sample = Timetable.importFromExcel("data/smaller_sample_schedule1.xlsx");
			EventActivityNetwork network = EventActivityNetwork.createNetwork(sample);
//			network.getStationDepartureNode("Rta", LocalDateTime.parse("2016-04-11T06:54"));
			
			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
			Path path = dijkstra.shortestPath(
					network.getStationDepartureNode("Nwk", LocalDateTime.parse("2016-04-11T06:47")), 
					network.getStationDepartureNode("Rtd", LocalDateTime.parse("2016-04-11T06:57")));
			System.out.println(path);
//			System.out.println(network);
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}
