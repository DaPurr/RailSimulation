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
public class Passenger implements Comparable<Passenger> {

	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private Station fromStation;
	private Station toStation;
	private final long id;
	
	/**
	 * Constructs a <code>Passenger</code> object.
	 * 
	 * @param checkIn		the check-in time
	 * @param checkOut		the check-out time
	 * @param fromStation	the origin station
	 * @param toStation		the destination station
	 */
	public Passenger(
			long id,
			LocalDateTime checkIn,
			LocalDateTime checkOut,
			Station fromStation,
			Station toStation) {
		this.checkIn = checkIn;
		this.checkOut = checkOut;
		this.fromStation = fromStation;
		this.toStation = toStation;
		this.id = id;
	}
	
	public long getId() {
		return id;
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

	@Override
	public int compareTo(Passenger o) {
		int res1 = checkIn.compareTo(o.checkIn);
		if (res1 != 0)
			return res1;
		int res2 = checkOut.compareTo(o.checkOut);
		if (res2 != 0)
			return res2;
		int res3 = fromStation.name().compareTo(o.fromStation.name());
		if (res3 != 0)
			return res3;
		int res4 = toStation.name().compareTo(o.toStation.name());
		if (res4 != 0)
			return res4;
		return Long.compare(this.id, o.id);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Passenger))
			return false;
		Passenger other = (Passenger) o;
//		boolean b1 = this.checkIn.equals(other.checkIn);
//		boolean b2 = this.checkOut.equals(other.checkOut);
//		boolean b3 = this.fromStation.equals(other.fromStation);
//		boolean b4 = this.toStation.equals(other.toStation);
//		return b1 && b2 && b3 && b4;
		return this.id == other.id;
	}
	
	@Override
	public int hashCode() {
//		return 7*checkIn.hashCode()
//				+ 11*checkOut.hashCode()
//				+ 13*fromStation.hashCode()
//				+ 17*toStation.hashCode();
		return Long.hashCode(id);
	}
	
}
