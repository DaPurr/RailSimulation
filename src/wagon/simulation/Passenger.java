package wagon.simulation;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;

/**
 * This class specifies a lone passenger solely in terms of 
 * check-in/check-out times, and origin and destination stations.
 * 
 * @author Nemanja Milovanovic
 *
 */
public class Passenger {

	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private Station fromStation;
	private Station toStation;
	
	/**
	 * Constructs a <code>Passenger</code> object.
	 * 
	 * @param checkIn		the check-in time
	 * @param checkOut		the check-out time
	 * @param fromStation	the origin station
	 * @param toStation		the destination station
	 */
	public Passenger(LocalDateTime checkIn,
			LocalDateTime checkOut,
			Station fromStation,
			Station toStation) {
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.fromStation = fromStation;
		this.toStation = toStation;
	}

	/**
	 * @return	returns check-in time
	 */
	public LocalDateTime getCheckInTime() {
		return checkIn;
	}

	/**
	 * @return	returns check-out time
	 */
	public LocalDateTime getCheckOutTime() {
		return checkOut;
	}

	/**
	 * @return	returns origin station
	 */
	public Station getFromStation() {
		return fromStation;
	}

	/**
	 * @return	returns destination station
	 */
	public Station getToStation() {
		return toStation;
	}
}
