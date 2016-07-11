package sandbox;

import java.io.IOException;
import java.util.*;

import wagon.data.CiCoData;
import wagon.simulation.*;

public class PiecewiseLinearProcessSandbox {

	public static void main(String[] args) {
		Options options = new Options("data/cico/ritten_20160209.csv", null, 2);
		CiCoData cicoData;
		try {
			cicoData = CiCoData
					.importRawData(
							"data/cico/ritten_20160209.csv",
							"data/cico/omzettabel_stations.csv",
							options);
			
			cicoData.getJourneySummary();
			
			Collection<Passenger> selectedPassengers = cicoData.getPassengersWithJourney("rta", "rtd");
			
			PiecewiseLinearProcess plml = new PiecewiseLinearProcess(
					selectedPassengers,		// arrivals
					7*60*60, 				// start time of window
					9*60*60, 				// end time of window
					5*60, 					// segment width
					//0.05, 					// left border point
					//0.10, 			// right border point
					0);						// seed
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
