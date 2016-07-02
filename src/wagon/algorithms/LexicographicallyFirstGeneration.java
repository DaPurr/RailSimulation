package wagon.algorithms;

import java.time.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import wagon.network.expanded.*;

public class LexicographicallyFirstGeneration implements RouteGeneration {

	private EventActivityNetwork network;
	private String from;
	private String to;
	private LocalDateTime checkInTime;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructs an object that is used to generate 
	 * a set of passenger journeys.
	 * 
	 * @param network	The <code>EventActivityNetwork</code> in which the journeys are 
	 * 					to be generated in.
	 */
	public LexicographicallyFirstGeneration(
			EventActivityNetwork network, 
			String from, 
			String to, 
			LocalDateTime checkInTime) {
		this.network = network;
		this.from = from;
		this.to = to;
		this.checkInTime = checkInTime;
		
		log.setLevel(Level.OFF);
	}
	
	public List<Path> generateRoutes() {
		List<Path> paths = new ArrayList<>();
		
		log.info("Start generating journeys...");
		
		// generate earliest arrival path
		BiCriterionDijkstra dijkstra = new BiCriterionDijkstra(network, Criterion.DISTANCE, Criterion.TRANSFER);
		Path path1 = dijkstra.lexicographicallyFirst(from, to, checkInTime);
		
		// if there is no earliest arrival path, there exists no path
		if (path1 == null) {
			paths.add(null);
			return paths;
		}
		
		paths.add(path1);
		if (path1.numberOfTransfers() == 0)
			return paths;
		
		// generate minimum number of transfers path
		dijkstra = new BiCriterionDijkstra(network, Criterion.TRANSFER, Criterion.DISTANCE);
		Path path2 = dijkstra.lexicographicallyFirst(from, to, checkInTime);
		paths.add(path2);
		
		log.info("...Finish generating routes");
		
		return paths;
	}
}
