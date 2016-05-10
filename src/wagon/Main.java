package wagon;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import wagon.algorithms.DijkstraShortestPath;
import wagon.algorithms.JourneyGeneration;
import wagon.algorithms.Path;
import wagon.network.expanded.EventActivityNetwork;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
		try {
			Timetable sample = Timetable.importFromXML("data/processed/full_dataset_export.xml");
			sample.export("data/processed/full_dataset_export2.xml");
			EventActivityNetwork network = EventActivityNetwork.createNetwork(sample);
			
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths1 = dijkstra.earliestArrivalPath("Vs", "Gn", LocalDateTime.parse("2016-04-11T15:30"));
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork2();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths2 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T10:50"));
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork3();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths3 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T11:59"));
			
			JourneyGeneration jgen = new JourneyGeneration(network);
			jgen.generateJourneys("Vs", "Gn", LocalDateTime.parse("2016-04-11T15:30"), 
					LocalDateTime.parse("2016-04-11T20:17"));
			
//			System.out.println(paths1);
//			System.out.println(paths2);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
