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
import wagon.data.CiCoData;
import wagon.algorithms.DefaultPath;
import wagon.network.expanded.EventActivityNetwork;
import wagon.simulation.Options;
import wagon.simulation.Report;
import wagon.simulation.SimModel;
import wagon.simulation.SystemState;
import wagon.timetable.Timetable;

public class Main {

	public static void main(String[] args) {
		try {
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/full_dataset.xlsx", 2);
//			Timetable sample = Timetable.importFromExcel("data/materieelplan/smaller_sample_schedule1.xlsx", 2);
//			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/smaller_sample_schedule1_export.xml");
			Timetable sample = Timetable.importFromXML("data/materieelplan/processed/full_dataset_day2_export.xml");
//			sample.export("data/materieelplan/processed/full_dataset_day2_export.xml");
//			sample.export("data/materieelplan/processed/smaller_sample_schedule1_day2_export.xml");
			EventActivityNetwork network = EventActivityNetwork.createNetwork(sample);
			
			CiCoData cicoData = CiCoData
					.importRawData(
							"data/cico/ritten_20160209.csv",
							"data/cico/omzettabel_stations.csv"); // hardcoded
			cicoData.exportPassengers("data/cico/processed/ritten_20160209_processed.csv");
			
//			long begin = System.nanoTime();
//			RouteGeneration rgen = new RouteGeneration(network);
//			List<DefaultPath> paths4 = rgen.generateRoutes(
//					"Nwk", 
//					"Rtd", 
//					LocalDateTime.parse("2016-04-11T06:45"), 
//					LocalDateTime.parse("2016-04-11T07:06"));
//			RouteSelection selectedLTLA = new SLTLARouteSelection(
//					LocalDateTime.parse("2016-04-11T06:45"),
//					LocalDateTime.parse("2016-04-11T07:06"),
//					10);
//			DefaultPath path4 = selectedLTLA.selectPath(paths4);
//			long end = System.nanoTime();
//			double duration = (end-begin)*1e-9;
//			System.out.println("Time: " + duration);
//			System.out.println(path4.representation());
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork2();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<DefaultPath> paths1 = dijkstra.earliestArrivalPath("Nwk", "Rtd", LocalDateTime.parse("2016-04-11T06:45"));
//			RouteSelection selectedLTLA1 = new SLTLARouteSelection(
//					LocalDateTime.parse("2016-04-11T06:46"),
//					LocalDateTime.parse("2016-04-11T06:58"),
//					10);
//			DefaultPath path8338930 = selectedLTLA1.selectPath(paths1);
//			System.out.println(path8338930.representation());
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork2();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<DefaultPath> paths2 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T10:50"));
			
//			EventActivityNetwork network = EventActivityNetwork.createTestNetwork3();
//			DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
//			List<DefaultPath> paths3 = dijkstra.earliestArrivalPath("A", "C", LocalDateTime.parse("2016-04-19T11:59"));
			
//			long begin = System.nanoTime();
//			RouteGeneration rgen = new RouteGeneration(network);
//			List<DefaultPath> paths3 = rgen.generateRoutes(
//					"Vs", 
//					"Gn", 
//					LocalDateTime.parse("2016-04-11T15:30"), 
//					LocalDateTime.parse("2016-04-11T20:17"));
//			RouteSelection selectedLTLA = new SLTLARouteSelection(
//					LocalDateTime.parse("2016-04-11T15:30"),
//					LocalDateTime.parse("2016-04-11T20:17"),
//					10);
//			DefaultPath path3 = selectedLTLA.selectPath(paths3);
//			long end = System.nanoTime();
//			double duration = (end-begin)*1e-9;
//			System.out.println("Time: " + duration);
			
//			long begin = System.nanoTime();
//			RouteGeneration rgen = new RouteGeneration(network);
//			List<DefaultPath> paths4 = rgen.generateRoutes(
//					"Rta", 
//					"Dt", 
//					LocalDateTime.parse("2016-04-11T07:55"), 
//					LocalDateTime.parse("2016-04-11T08:33"));
//			RouteSelection selectedLTLA = new SLTLARouteSelection(
//					LocalDateTime.parse("2016-04-11T07:55"),
//					LocalDateTime.parse("2016-04-11T08:33"),
//					10);
//			DefaultPath path4 = selectedLTLA.selectPath(paths4);
//			long end = System.nanoTime();
//			double duration = (end-begin)*1e-9;
//			System.out.println("Time: " + duration);
			
//			System.out.println(paths1);
//			System.out.println(paths2);
//			System.out.println(paths3);
//			System.out.println(paths4);
					
//			System.out.println(path3);
//			System.out.println(path4);
			
//			String rep2 = paths2.get(0).representation();
//			System.out.println(rep2);
//			DefaultPath newPath2 = network.textToPath(rep2);
//			System.out.println(newPath2);
			
//			String rep4 = "W3.9,W9.0," + path4.representation();
//			System.out.println(rep4);
//			DefaultPath newPath4 = network.textToPath(rep4);
//			System.out.println(newPath4);
//			System.out.println(newPath4.representation());
			
			Options options = new Options("data/cico/ritten_20160209.csv", null, 2);
//			options.setPathToProcessedGroupsData("data/cico/ritten_20160209_groups.csv");
			SimModel sim = new SimModel(sample, 
					network, 
					options);
			Report report = sim.start();
			System.out.println(report.summary());
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
