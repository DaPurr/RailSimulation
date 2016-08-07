package sandbox;

import java.io.IOException;
import java.util.Collection;

import wagon.data.CiCoData;
import wagon.simulation.Options;
import wagon.simulation.Passenger;
import wagon.simulation.ScaledPiecewiseLinearProcess;

public class ScaledPiecewiseLinearProcessSandbox {
	public static void main(String[] args) {
		Options options = new Options();
		options.setPathToCiCoData("data/cico/ritten_20160209.csv");
		options.setPathToStations("data/cico/omzettabel_stations.csv");
		options.setDayOfWeek(2);
		CiCoData cicoData;
		try {
			cicoData = CiCoData
					.importRawData(options);
			
			cicoData.getJourneySummary();
			
			Collection<Passenger> selectedPassengers = cicoData.getPassengersWithJourney("rta", "ut");
			
			ScaledPiecewiseLinearProcess plml = new ScaledPiecewiseLinearProcess(
					selectedPassengers,		// arrivals
					7*60*60,  // 7*60*60 + 2*5*60, 				// start time of window
					9*60*60,  // 7*60*60 + 3*5*60, 				// end time of window
					5*60, 					// segment width
					//0.05, 					// left border point
					//0.10, 			// right border point
					0);						// seed
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
