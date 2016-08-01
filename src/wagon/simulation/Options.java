package wagon.simulation;

import java.time.LocalTime;

/**
 * This class is used to pass options to a <code>SimModel</code> object.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class Options {

	private String pathToCiCoData;
	private String pathToStations;
	
	private int dayOfWeek;
	private int transferTime;
	private int checkInTimeCorrection;
	private int checkOutTimeCorrection;
	private LocalTime timeLowerBound;
	private LocalTime timeUpperBound;
	
	/**
	 * Constructs an object of class <code>Option</code>.
	 * 
	 * @param pathToRawCiCoData			the path to raw CiCo data
	 * @param pathToProcessedGroupsData	the path to processed passenger group data
	 * @param dayOfWeek					the day of the week
	 */
	public Options() {
		pathToCiCoData = null;
		pathToStations = null;
		
		transferTime = 0;
		checkInTimeCorrection = 0;
		checkOutTimeCorrection = 0;
		timeLowerBound = LocalTime.parse("00:00:00");
		timeUpperBound = LocalTime.parse("23:59:59");
	}
	
	public void setPathToCiCoData(String fileName) {
		pathToCiCoData = new String(fileName);
	}
	
	public String getPathToCiCoData() {
		return pathToCiCoData;
	}
	
	public void setPathToStations(String fileName) {
		pathToStations = new String(fileName);
	}
	
	public String getPathToStations() {
		return pathToStations;
	}
	
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	
	public void setCheckInTimeCorrection(int correction) {
		checkInTimeCorrection = correction;
	}
	
	public void setCheckOutTimeCorrection(int correction) {
		checkOutTimeCorrection = correction;
	}
	
	public int getCheckInTimeCorrection() {
		return checkInTimeCorrection;
	}
	
	public int getCheckOutTimeCorrection() {
		return checkOutTimeCorrection;
	}
	
	public void setCheckInLowerBound(LocalTime time) {
		timeLowerBound = time;
	}
	
	public void setCheckOutUpperBound(LocalTime time) {
		timeUpperBound = time;
	}
	
	public LocalTime getCheckInLowerBound() {
		return timeLowerBound;
	}
	
	public LocalTime getCheckOutUpperBound() {
		return timeUpperBound;
	}
	
	public int getTransferTime() {
		return transferTime;
	}
	
	public void setTransferTime(int transferTime) {
		this.transferTime = transferTime;
	}
}
