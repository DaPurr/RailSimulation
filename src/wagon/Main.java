package wagon;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import wagon.algorithms.DijkstraShortestPath;
import wagon.algorithms.RouteGeneration;
import wagon.algorithms.RouteSelection;
import wagon.algorithms.SLTLARouteSelection;
import wagon.algorithms.Path;
import wagon.network.expanded.EventActivityNetwork;
import wagon.simulation.SystemState;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
		try {
//			Timetable sample = Timetable.importFromExcel("data/full_dataset.xlsx");
//			Timetable sample = Timetable.importFromXML("data/processed/smaller_sample_schedule1_export.xml");
			Timetable sample = Timetable.importFromXML("data/processed/full_dataset_export.xml");
//			sample.export("data/processed/full_dataset_export.xml");
			EventActivityNetwork network = EventActivityNetwork.createNetwork(sample);
			
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths1 = dijkstra.earliestArrivalPath("Vs", "Gn", LocalDateTime.parse("2016-04-11T15:30"));
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork2();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths2 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T10:50"));
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork3();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<Path> paths3 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T11:59"));
			
//			long begin = System.nanoTime();
//			RouteGeneration rgen = new RouteGeneration(network);
//			List<Path> paths3 = rgen.generateRoutes(
//					"Vs", 
//					"Gn", 
//					LocalDateTime.parse("2016-04-11T15:30"), 
//					LocalDateTime.parse("2016-04-11T20:17"));
//			RouteSelection selectedLTLA = new SLTLARouteSelection(
//					LocalDateTime.parse("2016-04-11T15:30"),
//					LocalDateTime.parse("2016-04-11T20:17"),
//					10);
//			Path path3 = selectedLTLA.selectPath(paths3);
//			long end = System.nanoTime();
//			double duration = (end-begin)*1e-9;
//			System.out.println("Time: " + duration);
			
			long begin = System.nanoTime();
			RouteGeneration rgen = new RouteGeneration(network);
			List<Path> paths4 = rgen.generateRoutes(
					"Rta", 
					"Dt", 
					LocalDateTime.parse("2016-04-11T07:55"), 
					LocalDateTime.parse("2016-04-11T08:33"));
			RouteSelection selectedLTLA = new SLTLARouteSelection(
					LocalDateTime.parse("2016-04-11T07:55"),
					LocalDateTime.parse("2016-04-11T08:33"),
					10);
			Path path4 = selectedLTLA.selectPath(paths4);
			long end = System.nanoTime();
			double duration = (end-begin)*1e-9;
			System.out.println("Time: " + duration);
			
//			System.out.println(paths1);
//			System.out.println(paths2);
//			System.out.println(paths3);
//			System.out.println(paths4);
					
//			System.out.println(path3);
			System.out.println(path4);
			
//			String rep2 = paths2.get(0).representation();
//			System.out.println(rep2);
//			Path newPath2 = network.textToPath(rep2);
//			System.out.println(newPath2);
			
			String rep4 = path4.representation();
			System.out.println(rep4);
			Path newPath4 = network.textToPath(rep4);
			System.out.println(newPath4);
			System.out.println(newPath4.representation());
			
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (InvalidFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
