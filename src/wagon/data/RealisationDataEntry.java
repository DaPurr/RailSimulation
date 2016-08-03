package wagon.data;

import java.time.LocalDateTime;

import wagon.infrastructure.Station;
import wagon.rollingstock.TrainService;

public class RealisationDataEntry implements Comparable<RealisationDataEntry> {

	private int trainNr;
	
	private LocalDateTime realizedDepartureTime;
	private LocalDateTime plannedDepartureTime;
	private Station departureStation;
	
	private LocalDateTime realizedArrivalTime;
	private LocalDateTime plannedArrivalTime;
	private Station arrivalStation;
	
	private TrainService plannedComposition;
	private TrainService realizedComposition;
	
	public RealisationDataEntry(
			int trainNr, 
			LocalDateTime realizedDepartureTime, 
			LocalDateTime plannedDepartureTime, 
			Station departureStation, 
			LocalDateTime realizedArrivalTime, 
			LocalDateTime plannedArrivalTime, 
			Station arrivalStation, 
			TrainService plannedComposition, 
			TrainService realizedComposition) {
		this.trainNr = trainNr;
		
		this.realizedDepartureTime = realizedDepartureTime;
		this.plannedDepartureTime = plannedDepartureTime;
		this.departureStation = departureStation;
		
		this.realizedArrivalTime = realizedArrivalTime;
		this.plannedArrivalTime = plannedArrivalTime;
		this.arrivalStation = arrivalStation;
		
		this.plannedComposition = plannedComposition;
		this.realizedComposition = realizedComposition;
	}

	public int getTrainNr() {
		return trainNr;
	}

	public LocalDateTime getRealizedDepartureTime() {
		return realizedDepartureTime;
	}

	public LocalDateTime getPlannedDepartureTime() {
		return plannedDepartureTime;
	}

	public Station getDepartureStation() {
		return departureStation;
	}

	public LocalDateTime getRealizedArrivalTime() {
		return realizedArrivalTime;
	}

	public LocalDateTime getPlannedArrivalTime() {
		return plannedArrivalTime;
	}

	public Station getArrivalStation() {
		return arrivalStation;
	}

	public TrainService getPlannedComposition() {
		return plannedComposition;
	}

	public TrainService getRealizedComposition() {
		return realizedComposition;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof RealisationDataEntry))
			return false;
		RealisationDataEntry o = (RealisationDataEntry) other;
		boolean b1 = this.arrivalStation.equals(o.arrivalStation);
		boolean b2 = this.departureStation.equals(o.departureStation);
		boolean b3 = this.plannedArrivalTime.equals(o.plannedArrivalTime);
		boolean b4 = this.plannedComposition.equals(o.plannedComposition);
		boolean b5 = this.plannedDepartureTime.equals(o.plannedDepartureTime);
		boolean b6 = this.realizedArrivalTime.equals(o.realizedArrivalTime);
		boolean b7 = this.realizedComposition.equals(o.realizedComposition);
		boolean b8 = this.realizedDepartureTime.equals(o.realizedDepartureTime);
		boolean b9 = this.trainNr == o.trainNr;
		
		return b9 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8;
	}
	
	@Override
	public int hashCode() {
		int hc1 = Integer.hashCode(trainNr);
		int hc2 = arrivalStation.hashCode();
		int hc3 = departureStation.hashCode();
		int hc4 = plannedArrivalTime.hashCode();
		int hc5 = plannedComposition.hashCode();
		int hc6 = plannedDepartureTime.hashCode();
		int hc7 = realizedArrivalTime.hashCode();
		int hc8 = realizedComposition.hashCode();
		int hc9 = realizedDepartureTime.hashCode();
		
		return 7*hc1 + 13*hc2 + 17*hc3 + 23*hc4 + 29*hc5 + 31*hc6 + 37*hc7 + 41*hc8 + 43*hc9;
	}

	@Override
	public int compareTo(RealisationDataEntry o) {
		int res1 = this.plannedDepartureTime.compareTo(o.plannedDepartureTime);
		if (res1 != 0)
			return res1;
		int res2 = Integer.compare(this.trainNr, o.trainNr);
		if (res2 != 0)
			return res2;
		int res3 = this.realizedDepartureTime.compareTo(o.realizedDepartureTime);
		if (res3 != 0)
			return res3;
		int res4 = this.plannedArrivalTime.compareTo(o.plannedArrivalTime);
		if (res4 != 0)
			return res4;
		int res5 = this.realizedArrivalTime.compareTo(o.realizedArrivalTime);
		if (res5 != 0)
			return res5;
		int res6 = this.departureStation.compareTo(o.departureStation);
		if (res6 != 0)
			return res6;
		int res7 = this.arrivalStation.compareTo(o.arrivalStation);
		return res7;
	}
}
