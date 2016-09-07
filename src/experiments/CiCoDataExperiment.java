package experiments;

import java.io.IOException;

import wagon.data.CiCoData;
import wagon.simulation.Options;

public class CiCoDataExperiment {

	public static void main(String[] args) {
		Options options = new Options();
		options.setPathToCiCoData("data/cico/ritten_20160209.csv");
		options.setPathToStations("data/cico/omzettabel_stations.csv");
		options.setDayOfWeek(2);
		options.setSeed(5678);
		options.setSegmentWidth(1); // in minutes
		options.setTransferTime(1);
		options.setNumberofProcessors(4);
		options.setPhi(1.0);
		
		try {
			CiCoData cicoData = CiCoData.importRawData(options);
			System.out.println("Nr. passengers: " + cicoData.getPassengers().size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
