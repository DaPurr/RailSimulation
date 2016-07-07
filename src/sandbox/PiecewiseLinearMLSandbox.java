package sandbox;

import java.io.IOException;
import java.util.*;

import wagon.data.CiCoData;
import wagon.simulation.*;

public class PiecewiseLinearMLSandbox {

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
		
//			double[] arrivals = {
//					0.0097627,
//					0.030315377,
//					0.129961329,
//					0.156539469,
//					0.170148382,
//					0.391750233,
//					0.767721537,
//					0.823352886,
//					0.855340197,
//					0.883324401,
//					0.912066576,
//					0.966913315,
//					0.980600104,
//					1.004127202,
//					1.063878059,
//					1.137367289,
//					1.171494862,
//					1.31326534,
//					1.428064365,
//					1.504452291,
//					1.672129347,
//					1.742760704,
//					1.889040762,
//					2.084936088,
//					2.149134734,
//					2.216723715,
//					2.24920814,
//					2.551867958,
//					2.768041691,
//					2.903061607,
//					2.941556031,
//					3.017026547,
//					3.275275518,
//					3.297357034,
//					3.340622475,
//					3.525805003,
//					4.515484639,
//					4.846507329,
//					4.867785971,
//					5.265388322,
//					5.31417816,
//					5.363685595,
//					5.374948747,
//					5.414395805,
//					5.929116094,
//					6.649369654,
//					6.673665333,
//					6.854851646,
//					6.966926361,
//					7.173430791,
//					7.207426806,
//					7.278484114,
//					7.369410876,
//					7.551723252,
//					7.767266825,
//					8.030655932,
//					8.082331663,
//					8.20503194,
//					8.271241631,
//					8.29020212,
//					8.351311135,
//					8.491719375,
//					8.58696958,
//					8.821424867,
//					8.949163464,
//					9.134416765,
//					9.228380679,
//					9.234163082,
//					9.25085606,
//					9.602579259,
//					9.636562569,
//					9.718569332,
//					9.928841918,
//					9.952281311,
//					10.00767781,
//					10.02798843,
//					10.55793634,
//					10.70608599,
//					10.89659375,
//					11.09565813,
//					11.33336451,
//					12.09404373,
//					12.59057762,
//					13.00538366,
//					13.60510858,
//					13.72276441,
//					13.79517142,
//					13.84356109,
//					14.02858064,
//				};
			
			PiecewiseLinearProcess plml = new PiecewiseLinearProcess(
					selectedPassengers,		// arrivals
					7*60*60, 				// start time of window
					9*60*60, 				// end time of window
					5*60, 					// segment width
//					0.05, 					// left border point
//					0.05, 			// right border point
					0);						// seed
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
