package wagon.algorithms;

import java.util.Collection;

public class EarliestArrivalSelector implements RouteSelection {

	@Override
	public Path selectPath(Collection<Path> paths) {
		Path bestPath = null;
		double bestTravelTime = Double.POSITIVE_INFINITY;
		
		for (Path path : paths) {
			double currentTravelTime = path.travelTime();
			if (currentTravelTime < bestTravelTime)
				bestPath = path;
		}
		return bestPath;
	}

}
