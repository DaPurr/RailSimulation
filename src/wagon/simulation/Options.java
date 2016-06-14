package wagon.simulation;

import java.time.LocalTime;

/**
 * This class is used to pass options to a <code>SimModel</code> object.
 * 
 * <p>
 * As of now, <code>Option</code> contains only required options. It 
 * is necessary to pass either a path to raw CiCo data, or to a file 
 * containing passenger group data processed from raw CiCo data. Note 
 * that the latter allows for much shorter computation times. Raw CiCo 
 * data can be obtained through the <code>ExportPassengerGroups()</code> 
 * method of the <code>SimModel</code> class. 
 * 
 * @author Nemanja Milovanovic
 *
 */
public class Options {

	private String pathToRawCiCoData;
	private String pathToProcessedGroupsData;
	private int dayOfWeek;
	private int checkInTimeCorrection;
	private int checkOutTimeCorrection;
	private LocalTime timeLowerBound;
	private LocalTime timeUpperBound;
	
	/**
	 * Constructs an object of class <code>Option</code>.
	 * 
	 * <p>
	 * Note that that there is need of only one input file, so either 
	 * <code>pathToRawCiCoData</code> or <code>pathToProcessedGroupsData</code> 
	 * is expected to be <code>null</code>. If none of the arguments are, then 
	 * the data regarding processed group data is used.
	 * 
	 * @param pathToRawCiCoData			the path to raw CiCo data
	 * @param pathToProcessedGroupsData	the path to processed passenger group data
	 * @param dayOfWeek					the day of the week
	 */
	public Options(
			String pathToRawCiCoData,
			String pathToProcessedGroupsData,
			int dayOfWeek) {
		this.pathToProcessedGroupsData = pathToProcessedGroupsData;
		this.pathToRawCiCoData = pathToRawCiCoData;
		this.dayOfWeek = dayOfWeek;
		checkInTimeCorrection = 0;
		checkOutTimeCorrection = 0;
		timeLowerBound = LocalTime.parse("00:00:00");
		timeUpperBound = LocalTime.parse("23:59:59");
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
}
