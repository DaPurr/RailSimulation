package wagon.algorithms;

import java.time.*;
import java.util.*;
import java.util.logging.Logger;

import wagon.network.expanded.EventActivityNetwork;

/**
 * This class employs shortest-path queries in order to generate a set of journeys, 
 * according to Van Der Hurk (2015). The variant is used which generates eariest 
 * arrival paths.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class JourneyGeneration {

	private EventActivityNetwork network;
	
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * Constructs <code>JourneyGeneration</code> object that is used to generate 
	 * a set of passenger journeys.
	 * 
	 * @param network	The <code>EventActivityNetwork</code> in which the journeys are 
	 * 					to be generated in.
	 */
	public JourneyGeneration(EventActivityNetwork network) {
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
		
		return paths;
	}
}
