package wagon.simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;

public class PiecewiseConstantProcess implements ArrivalProcess {
	
	private final int horizon = 24*60*60; // time horizon in seconds
	
	private Collection<Passenger> passengers;
	private int nSegments;
	private int segmentWidth;
	private double[] midpoints;
	private double lambdaUB;
	
	private MersenneTwister random = new MersenneTwister();
	
	public PiecewiseConstantProcess(Collection<Passenger> passengers, int segmentWidth) {
		this.passengers = passengers;
		this.segmentWidth = segmentWidth;
		nSegments = (int) Math.ceil((double)horizon/segmentWidth);
		
		midpoints = determineMidpoints(passengers, nSegments);
		lambdaUB = max(midpoints);
	}
	
	private double max(double[] x) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (double v : x) {
			if (v > maxValue)
				maxValue = v;
		}
		return maxValue;
	}
	
	private double[] determineMidpoints(Collection<Passenger> passengers, int nSegments) {
		List<Passenger> sortedPassengers = new ArrayList<>(passengers);
		Collections.sort(sortedPassengers);
		double[] midpoints = new double[nSegments];
		Arrays.fill(midpoints, 0.0);
		for (Passenger passenger : sortedPassengers) {
			LocalDateTime checkInTime = passenger.getCheckInTime();
			int intCheckInTime = checkInTime.toLocalTime().toSecondOfDay();
			int currentSegment = intCheckInTime / segmentWidth;
			// in theory a check-out could happen at the last second,
			// resulting in the modulo to be 1 higher than we want...
			// this wouldn't be a problem if the time were continuous
			if (currentSegment == midpoints.length)
				currentSegment = midpoints.length-1;
			midpoints[currentSegment]++;
		}
		for (int i = 0; i < midpoints.length; i++) {
			int width = segmentWidth;
			if (i == midpoints.length-1)
				width = horizon - i*segmentWidth;
			midpoints[i] = midpoints[i]/width;
		}
		return midpoints;
	}

	@Override
	public int generateArrival(int time) {
		double currTime = time;
		
		ExponentialDistribution exponential = new ExponentialDistribution(1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		int segment = (int) Math.floor(currTime/segmentWidth);
		
		double acceptProb = midpoints[segment]/lambdaUB;
		double r = random.nextDouble();
		while (r > acceptProb && currTime <= horizon) {
			randomExponential = exponential.sample();
			currTime += randomExponential;
			segment = (int) Math.floor(currTime/segmentWidth);
			if (segment >= midpoints.length)
				segment = midpoints.length-1;
			
			acceptProb = midpoints[segment]/lambdaUB;
			r = random.nextDouble();
		}
		int ceiledArrivalTime = (int) Math.ceil(currTime);
		return ceiledArrivalTime;
	}
	
	public List<Integer> generateArrivalsFromProcess() {
		List<Integer> events = new ArrayList<>();
		int currTime = 0;
		while (currTime < horizon) {
			int r = generateArrival(currTime);
			currTime = r;
			events.add(r);
		}
		return events;
	}
	
	public void exportArrivals(int window, String fileName) throws IOException {
		int nrWindows = (int) Math.ceil((double)horizon/window);
		int[] counts = new int[nrWindows];
		Arrays.fill(counts, 0);
		List<Integer> events = generateArrivalsFromProcess();
		for (int v : events) {
			counts[v/segmentWidth]++;
		}
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int v : counts) {
			bw.write(String.valueOf(v));
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}

}
