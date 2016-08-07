package sandbox;

import java.io.IOException;
import java.util.*;

import wagon.data.CiCoData;
import wagon.simulation.*;

public class HybridArrivalProcessSandbox {

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
			
			Collection<Passenger> selectedPassengers = cicoData.getPassengersWithJourney("rta", "rtd");
			
			long startTime = System.nanoTime();
			HybridArrivalProcess hap = new HybridArrivalProcess(
					selectedPassengers, 
					0, 
					24*60*60, 
					5*60, 
					0);
			long endTime = System.nanoTime();
			double duration = (endTime-startTime)*1e-9;
			System.out.println("Computation time: " + duration + " s");
			
			List<Double> arrivals = hap.generateArrivalsFromProcess(24*60*60);
			System.out.println("Original passengers: " + selectedPassengers.size());
			System.out.println("Generated passengers: " + arrivals.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
