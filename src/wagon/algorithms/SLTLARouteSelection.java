package wagon.algorithms;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This class implements the Least Transfers, Last Arrival selection rule, 
 * as described in Van Der Hurk (2015). 
 * 
 * This rule entails selecting routes such that the sum of the difference 
 * in departure and check-in, and arrival and check-out is less or equal 
 * to x minutes. If a tie occurs, the path is selected with the least 
 * number of transfer, the latest arrival, and the first departure as 
 * criteria (in that order).
 * 
 * @author Nemanja Milovanovic
 *
 */
public class SLTLARouteSelection implements RouteSelection {
	
	private LocalDateTime checkInTime;
	private LocalDateTime checkOutTime;
	private int x;
	
	public SLTLARouteSelection(
			LocalDateTime checkInTime,
			LocalDateTime checkOutTime,
			int x) {
		this.checkInTime = checkInTime;
		this.checkOutTime = checkOutTime;
		this.x = x;
	}

	@Override
	public DefaultPath selectPath(Collection<DefaultPath> paths) {
		
		// apply the selection rule
		List<DefaultPath> ties = new ArrayList<>();
		for (DefaultPath p : paths) {
			LocalDateTime departureTime = p.departureTime();
			LocalDateTime arrivalTime = p.arrivalTime();
			Duration departureCheckInDiff = Duration.between(departureTime, checkInTime).abs();
			Duration arrivalCheckOutDiff = Duration.between(arrivalTime, checkOutTime).abs();
			long sum = departureCheckInDiff.toMinutes() + arrivalCheckOutDiff.toMinutes();
			if (sum <= x)
				ties.add(p);
		}
		
		if (ties.size() == 1)
			return ties.get(0);
		// tie-break SLTLA paths
		else if (ties.size() > 1) {
			return tieBreakerPath(ties);
		}
		// tie-break all paths
		else
			return tieBreakerPath(paths);
	}
	
	private DefaultPath tieBreakerPath(Collection<DefaultPath> ties) {
		// least transfers
		List<DefaultPath> leastTransfers = new ArrayList<>();
		int bestTransfers = Integer.MAX_VALUE;
		for (DefaultPath p : ties) {
			int countTransfers = p.countTransfers();
			if (countTransfers < bestTransfers) {
				leastTransfers = new ArrayList<>();
				leastTransfers.add(p);
			} else if (countTransfers == bestTransfers) {
				leastTransfers.add(p);
			}
		}
		if (leastTransfers.size() == 1)
			return leastTransfers.get(0);
		
		// latest arrival
		List<DefaultPath> latestArrivals = new ArrayList<>();
		LocalDateTime bestArrival = null;
		for (DefaultPath p : leastTransfers) {
			if (bestArrival == null) {
				latestArrivals.add(p);
				bestArrival = p.arrivalTime();
			} else if (p.arrivalTime().compareTo(bestArrival) > 0) {
				bestArrival = p.arrivalTime();
				latestArrivals = new ArrayList<>();
				latestArrivals.add(p);
			} else if (p.arrivalTime().compareTo(bestArrival) == 0)
				latestArrivals.add(p);
		}
		if (latestArrivals.size() == 1)
			return latestArrivals.get(0);
		return firstDeparture(latestArrivals);
	}
	
	// retrieve the path having the earliest departure time
	// from a collection of paths
	private DefaultPath firstDeparture(Collection<DefaultPath> paths) {
		DefaultPath bestPath = null;
		for (DefaultPath p : paths) {
			if (bestPath == null)
				bestPath = p;
			else if (p.departureTime().compareTo(bestPath.departureTime()) < 0)
				bestPath = p;
		}
		return bestPath;
	}

}
