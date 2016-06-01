package wagon.algorithms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.network.expanded.*;
import wagon.timetable.ScheduledTrip;

public class DefaultPath{

	private double totalCost;
	private List<WeightedEdge> edges;
	
	public DefaultPath() {
		edges = new ArrayList<>();
	}
	
	public DefaultPath(DefaultPath path) {
		totalCost = 0;
		edges = new ArrayList<>();
		for (WeightedEdge edge : path.edges) {
			addEdge(edge);
		}
	}
	
	public void addEdge(WeightedEdge e) {
		edges.add(e);
		totalCost += e.weight();
	}
	
	public void removeEdge(WeightedEdge e) {
		edges.remove(e);
		totalCost -= e.weight();
	}
	
	public double weight() {
		return totalCost;
	}

	public Node startNode() {
		if (edges.isEmpty())
			return null;
		Node node = edges.get(0).source();
		return node;
	}

	public Node endNode() {
		if (edges.isEmpty())
			return null;
		WeightedEdge edge = edges.get(edges.size()-1);
		Node node = edge.target();
		return node;
	}

	public List<WeightedEdge> edges() {
		return new ArrayList<>(edges);
	}
	
	public String toString() {
		String s = "";
		for (int i = 0; i < edges.size(); i++) {
			WeightedEdge edge = edges.get(i);
			if (edge instanceof WaitEdge)
				continue;
			TripEdge tripEdge = (TripEdge) edge;
			ScheduledTrip trip = tripEdge.trip();
			s += "departure:\t" + getClockTime(trip.departureTime()) + "\t" + trip.fromStation().name() + 
					"\t(" + trip.composition().type() + ")\n";
			s += "arrival:\t" + getClockTime(trip.arrivalTime()) + "\t" + trip.toStation().name() + 
					"\t(" + trip.composition().type() + ")\n";
		}
		return s;
	}
	
	/**
	 * Reverses this path.
	 */
	public void reverse() {
		Collections.reverse(edges);
	}
	
	private String getClockTime(LocalDateTime time) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		return time.format(formatter);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DefaultPath))
			return false;
		DefaultPath o = (DefaultPath) other;
		return this.edges.equals(o.edges) &&
				this.weight() == o.weight();
	}
	
	@Override
	public int hashCode() {
		return 7*edges.hashCode() + 13*Double.valueOf(totalCost).hashCode();
	}

	public LocalDateTime departureTime() {
		return edges.get(0).source().trip().departureTime();
	}

	public LocalDateTime arrivalTime() {
		return edges.get(edges.size()-1).target().trip().arrivalTime();
	}

	public int countTransfers() {
		int currentID = Integer.MIN_VALUE;
		int count = 0;
		for (WeightedEdge e : edges) {
			EventNode event = e.source();
			int compID = event.trip().composition().id();
			if (compID != currentID) {
				count++;
				currentID = compID;
			}
		}
		return count;
	}

	public String representation() {
		String s = "";
		boolean b = false;
		for (WeightedEdge edge : edges) {
			if (b) {
				s += ",";
			}
			if (edge instanceof WaitEdge) {
				s += parseWaitEdge((WaitEdge) edge);
			} else {
				s += parseTripEdge((TripEdge) edge);
			}
			b = true;
		}
		return s;
	}
	
	private String parseWaitEdge(WaitEdge e) {
		String s = "W";
		s += e.weight();
		return s;
	}
	
	private String parseTripEdge(TripEdge e) {
		String s = "D";
		ScheduledTrip trip = e.trip();
		s += trip.fromStation().name();
		s += trip.departureTime().toString();
		s += ",A";
		s += trip.toStation().name();
		s += trip.arrivalTime().toString();
		
		return s;
	}

}
