package wagon.simulation;

public class Options {

	private String pathToRawCiCoData;
	private String pathToProcessedGroupsData;
	private int dayOfWeek;
	
	public Options() {
		pathToProcessedGroupsData = null;
		pathToRawCiCoData = null;
		dayOfWeek = 1;
	}
	
	public void setDayOfWeek(int day) {
		dayOfWeek = day;
	}
	
	public void setpathToRawCiCoData(String fileName) {
		pathToRawCiCoData = fileName;
	}
	
	public void setPathToProcessedGroupsData(String fileName) {
		pathToProcessedGroupsData = fileName;
	}
	
	public String getPathToRawCiCoData() {
		return pathToRawCiCoData;
	}
	
	public String getPathToProcessedGroupsData() {
		return pathToProcessedGroupsData;
	}
	
	public int getDayOfWeek() {
		return dayOfWeek;
	}
}
