package wagon.algorithms;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wagon.network.Node;
import wagon.network.WeightedEdge;
import wagon.network.expanded.TripEdge;
import wagon.network.expanded.WaitEdge;
import wagon.timetable.ScheduledTrip;

public class DefaultPath implements Path {

	private double totalCost;
	private List<WeightedEdge> edges;
	
	DefaultPath() {
		edges = new ArrayList<>();
	}
	
	DefaultPath(DefaultPath path) {
		totalCost = 0;
		edges = new ArrayList<>();
		for (WeightedEdge edge : path.edges) {
			addEdge(edge);
		}
	}
	
	void addEdge(WeightedEdge e) {
		edges.add(e);
		totalCost += e.weight();
	}
	
	void removeEdge(WeightedEdge e) {
		edges.remove(e);
		totalCost -= e.weight();
	}
	
	@Override
	public double weight() {
		return totalCost;
	}

	@Override
	public Node startNode() {
		if (edges.isEmpty())
			return null;
		Node node = edges.get(0).source();
		return node;
	}

	@Override
	public Node endNode() {
		if (edges.isEmpty())
			return null;
		WeightedEdge edge = edges.get(edges.size()-1);
		Node node = edge.target();
		return node;
	}

	@Override
	public List<WeightedEdge> edges() {
		return new ArrayList<>(edges);
	}
	
	@Override
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

	@Override
	public LocalDateTime departureTime() {
		return edges.get(0).source().trip().departureTime();
	}

	@Override
	public LocalDateTime arrivalTime() {
		return edges.get(edges.size()-1).target().trip().arrivalTime();
	}

}
