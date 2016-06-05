package wagon.simulation;

public class Options {

	private String pathToRawCiCoData;
	private String pathToProcessedGroupsData;
	
	public Options() {
		pathToProcessedGroupsData = null;
		pathToRawCiCoData = null;
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
}
