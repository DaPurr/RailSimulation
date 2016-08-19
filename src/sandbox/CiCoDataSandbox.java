package sandbox;

import java.io.IOException;

import wagon.data.CiCoData;
import wagon.simulation.Options;

public class CiCoDataSandbox {

	public static void main(String[] args) {
		Options options = new Options();
		options.setDayOfWeek(2);
		options.setPathToStations("data/cico/omzettabel_stations.csv");
		options.setPathToCiCoData("data/cico/ritten_20160112.csv");
		String station = "rtd";
		try {
			CiCoData cicoData = CiCoData.importRawData(options);
//			cicoData.exportEmpiricalArrivalRateOfCheckInStation(
//					station, 
//					5*60, 
//					"data/cico/export/20160112_" + station + ".csv");
			
			options.setPathToCiCoData("data/cico/ritten_20160209.csv");
			cicoData = CiCoData.importRawData(options);
//			cicoData.exportEmpiricalArrivalRateOfCheckInStation(
//					station, 
//					5*60, 
//					"data/cico/export/20160209_"+station+".csv");
			
//			options.setPathToCiCoData("data/cico/ritten_20160315.csv");
//			cicoData = CiCoData.importRawData(options);
//			cicoData.exportEmpiricalArrivalRateOfCheckInStation(
//					station, 
//					5*60, 
//					"data/cico/export/20160315_"+station+".csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
