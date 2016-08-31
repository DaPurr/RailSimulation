package sandbox;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wagon.algorithms.*;
import wagon.network.expanded.*;
import wagon.timetable.*;

public class RoutingSandbox {

	public static void main(String[] args) {
		try {
			Timetable timetable = Timetable.importFromXML("data/materieelplan/processed/full_dataset2_export.xml");
			EventActivityNetwork network = EventActivityNetwork.createTransferNetwork(timetable, 2, 1);
			
			RouteGeneration rgen = new LexicographicallyFirstGeneration(network, "rta", "dt", LocalTime.parse("12:00"));
			List<Path> generatedPaths = rgen.generateRoutes();
			RouteSelection selector = new EarliestArrivalSelector();
			Path path = selector.selectPath(generatedPaths);
			System.out.println(path);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

}