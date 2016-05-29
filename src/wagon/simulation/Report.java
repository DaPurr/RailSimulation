package wagon.simulation;

import java.io.IOException;

public class Report {

	private SystemState state;
	
	public Report(SystemState state) {
		this.state = state;
	}
	
	public String summary() {
		String s = "";
		
		return s;
	}
	
	public void exportToFile(String file_name) throws IOException {
		
	}
}
