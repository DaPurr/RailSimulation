package wagon.data;

import java.io.*;
import java.util.Set;

import wagon.simulation.PassengerGroup;

public class CiCoData {

	private Set<PassengerGroup> groups;
	
	private CiCoData() {
		// TODO Auto-generated constructor stub
	}
	
	public static CiCoData importRawData(String file_name) throws IOException {
		if (!file_name.matches(".*\\.csv"))
			throw new IllegalArgumentException("File needs to be excel format.");
		CiCoData cicoData = new CiCoData();
		File file = new File(file_name);
		BufferedReader br = new BufferedReader(
				new FileReader(file));
		String line = br.readLine();
		line = br.readLine();
		while (line != null) {
			String[] parts = line.split(",");
		}
		br.close();
		
		return cicoData;
	}
}
