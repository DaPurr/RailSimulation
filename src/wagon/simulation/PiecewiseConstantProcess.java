package wagon.simulation;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class PiecewiseConstantProcess implements ArrivalProcess {
	
	private final int horizon = 24*60*60; // time horizon in seconds
	
	private List<Passenger> passengers;
	private int nSegments;
	private int segmentWidth;
	private double[] midpoints;
	private double lambdaUB;
	
	private MersenneTwister random;
	
	public PiecewiseConstantProcess(Collection<Passenger> passengers, int segmentWidth, int seed) {
		this.passengers = new ArrayList<>(passengers);
		Collections.sort(this.passengers);
		this.segmentWidth = segmentWidth;
		nSegments = (int) Math.ceil((double)horizon/segmentWidth);
		random = new MersenneTwister(seed);
		
		midpoints = determineMidpoints(nSegments);
		lambdaUB = max(midpoints);
	}
	
	public PiecewiseConstantProcess(Collection<Passenger> passengers, int segmentWidth) {
		this(passengers, segmentWidth, 0);
		random = new MersenneTwister();
	}
	
	private double max(double[] x) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (double v : x) {
			if (v > maxValue)
				maxValue = v;
		}
		return maxValue;
	}
	
	private double[] determineMidpoints(int nSegments) {
		double[] midpoints = new double[nSegments];
//		Arrays.fill(midpoints, 0.0);
		for (Passenger passenger : passengers) {
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
	
	public void exportDrawsFromProcess(int window, String fileName) throws IOException {
		int nrWindows = (int) Math.ceil((double)horizon/window);
		int[] counts = new int[nrWindows];
		Arrays.fill(counts, 0);
		List<Integer> events = generateArrivalsFromProcess();
		for (int v : events) {
			if (v < horizon)
				counts[v/window]++;
		}
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < counts.length; i++) {
			int v = counts[i];
			int xAxis = i*window + window/2;
			bw.write(xAxis + "," + String.valueOf( (double)v/window ));
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}
	
	public void exportArrivalRate(String fileName) throws IOException {
		File file = new File(fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		for (int i = 0; i < horizon; i++) {
			bw.write(i + "," + midpoints[i/segmentWidth]);
			bw.newLine();
		}
		bw.flush();
		bw.close();
	}
	
	public double kolmogorovSmirnovTest() {
		List<Double> standardExponentials = new ArrayList<>();
		
		// transform arrivals into standard uniform variables
		// at least, under null-hypothesis
		List<List<Double>> arrivalTimes = new ArrayList<>();
		int nrWindows = (int) Math.ceil((double)horizon/segmentWidth);
		for (int i = 0; i < nrWindows; i++) {
			arrivalTimes.add(new ArrayList<>());
		}
		for (Passenger passenger : passengers) {
			LocalDateTime checkInTime = passenger.getCheckInTime();
			int intCheckInTime = checkInTime.toLocalTime().toSecondOfDay();
			int currentSegment = intCheckInTime / segmentWidth;
			// in theory a check-out could happen at the last second,
			// resulting in the modulo to be 1 higher than we want...
			// this wouldn't be a problem if the time were continuous
			if (currentSegment == midpoints.length)
				currentSegment = midpoints.length-1;
			double unroundedTime = intCheckInTime + random.nextDouble();
			arrivalTimes.get(currentSegment).add(unroundedTime);
		}
		// done with pre-work, do actual calculations
		for (int i = 0; i < arrivalTimes.size(); i++) {
			List<Double> innerTimes = arrivalTimes.get(i);
			for (int j = 0; j < innerTimes.size(); j++) {
				double r_left = innerTimes.size() + 1 - j;
				double r_log_num = segmentWidth - innerTimes.get(j);
				double t_i_jmin1 = 0;
				if (j-1 >= 0)
					t_i_jmin1 = innerTimes.get(j-1);
				double r_log_den = segmentWidth-t_i_jmin1;
				double r = r_left * (-Math.log(r_log_num/r_log_den));
				standardExponentials.add(r);
			}
		}
		// done calculating
		double[] exps = new double[standardExponentials.size()];
		for (int i = 0; i < exps.length; i++) {
			exps[i] = standardExponentials.get(i);
		}
		
		KolmogorovSmirnovTest kstest = new KolmogorovSmirnovTest();
		double pVal = kstest.kolmogorovSmirnovTest(new ExponentialDistribution(1.0), exps);
		return pVal;
	}

}
