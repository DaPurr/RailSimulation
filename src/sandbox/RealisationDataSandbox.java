package sandbox;

import java.io.IOException;

import wagon.data.RealisationData;

public class RealisationDataSandbox {

	public static void main(String[] args) {
		try {
			RealisationData rdata = RealisationData.importFromFile(
					"data/realisatie/DM_INZET_MATERIEEL_CAP.csv", 
					"data/realisatie/train_numbers.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
