package wagon.algorithms;

import java.time.*;
import java.util.*;
import java.util.logging.Logger;

import wagon.infrastructure.*;
import wagon.network.expanded.*;

/**
 * This class employs shortest-path queries in order to generate a set of journeys, 
 * according to Van Der Hurk (2015). The variant is used which generates eariest 
 * arrival paths.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class RouteGeneration {

	private EventActivityNetwork network;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructs <code>JourneyGeneration</code> object that is used to generate 
	 * a set of passenger journeys.
	 * 
	 * @param network	The <code>EventActivityNetwork</code> in which the journeys are 
	 * 					to be generated in.
	 */
	public RouteGeneration(EventActivityNetwork network) {
		this.network = network;
	}
	
	/**
	 * @param from		abbreviated name of the origin station
	 * @param to		abreviated name of the destination station
	 * @param checkIn	check-in time
	 * @param checkOut	check-out time
	 * @return			set of earliest arriving journeys
	 */
	public List<Path> generateJourneys(String from, String to, 
			LocalDateTime checkIn, LocalDateTime checkOut) {
		List<Path> paths = new ArrayList<>();
		log.info("Start extracting departure nodes...");
		List<DepartureNode> departureNodes = new ArrayList<>(network.getDeparturesByStation(new Station(from)));
		Collections.sort(departureNodes);
		log.info("...Finished extracting departure nodes");
		
		DijkstraShortestPath dijkstra = new DijkstraShortestPath(network);
		
		log.info("Start generating journeys...");
		for (DepartureNode departure : departureNodes) {
			if (departure.trip().departureTime().compareTo(checkIn) < 0)
				continue;
			// departure time is after check-in time, so process
			List<Path> journeys = dijkstra.earliestArrivalPath(departure, to);
			if (journeys.get(0).arrivalTime().compareTo(checkOut) > 0)
				break;
			paths.addAll(journeys);
		}
		log.info("...Finish generating journeys");
		
		return paths;
	}
}
