package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;

public class PiecewiseConstantProcess implements ArrivalProcess {
	
	private final int horizon = 24*60*60; // time horizon in seconds
	
	private List<Double> passengers;
	private int nSegments;
	private int segmentWidth;
//	private double[] midpoints;
	private double[] intercept;
	private double[] w;
	
	private MersenneTwister random;
	
	public PiecewiseConstantProcess(Collection<Passenger> passengers, int segmentWidth, long seed) {
		random = new MersenneTwister(seed);
		
		this.passengers = new ArrayList<>();
		for (Passenger passenger : passengers) {
			double time = passenger.getCheckInTime().toLocalTime().toSecondOfDay() + random.nextDouble();
			if (time < horizon)
				this.passengers.add(time);
		}
		
		Collections.sort(this.passengers);
		this.segmentWidth = segmentWidth;
		nSegments = (int) Math.ceil((double)horizon/segmentWidth);
		intercept = new double[nSegments];
		
		w = new double[nSegments+1];
		// make knots
		for (int i = 0; i < nSegments+1; i++) {
			w[i] = i*segmentWidth;
		}
		
		estimateLambda();
		
//		midpoints = determineMidpoints(nSegments);
	}
	
	private void estimateLambda() {
		for (double arrival : passengers) {
			int segment = (int) Math.floor(arrival/segmentWidth);
			intercept[segment] += 1.0;
		}
		for (int i = 0; i < intercept.length; i++) {
			intercept[i] /= segmentWidth;
		}
	}
	
	public double getLambdaUpperBound() {
		return max(intercept);
	}
	
	private double max(double[] x) {
		double maxValue = Double.NEGATIVE_INFINITY;
		for (double v : x) {
			if (v > maxValue)
				maxValue = v;
		}
		return maxValue;
	}
	
	public double logLikelihood() {
		List<List<Double>> matrixArrivals = new ArrayList<>();
		
		for (int i = 0; i < nSegments; i++)
			matrixArrivals.add(new ArrayList<>());
		
		// divide arrivals into segments
		for (double val : passengers) {
			int index = (int) Math.floor(val/segmentWidth);
			matrixArrivals.get(index).add(val);
		}
		
		int min_arrivals = Integer.MAX_VALUE;
		for (int i = 0; i < matrixArrivals.size(); i++) {
			if (matrixArrivals.get(i).size() < min_arrivals)
				min_arrivals = matrixArrivals.get(i).size();
		}
		
		double result = 0.0;
		for (int i = 0; i < nSegments; i++) {
			double a_i = intercept[i];
			double b_i = 0.0;
			double w_i = w[i+1];
			double w_i_1 = w[i];
			
			// first expression
			List<Double> segmentArrivals = matrixArrivals.get(i);
			for (double t_j : segmentArrivals) {
				double term = a_i + b_i*t_j;
				result += Math.log(term);
			}
			
			// second expression
			result -= ( a_i*(w_i - w_i_1) + b_i*(0.5*w_i*w_i - 0.5*w_i_1*w_i_1) );
		}
		return result;
	}
	
//	private double[] determineMidpoints(int nSegments) {
//		double[] midpoints = new double[nSegments];
//		for (Passenger passenger : passengers) {
//			LocalDateTime checkInTime = passenger.getCheckInTime();
//			int intCheckInTime = checkInTime.toLocalTime().toSecondOfDay();
//			int currentSegment = intCheckInTime / segmentWidth;
//			// in theory a check-out could happen at the last second,
//			// resulting in the modulo to be 1 higher than we want...
//			// this wouldn't be a problem if the time were continuous
//			if (currentSegment == midpoints.length)
//				currentSegment = midpoints.length-1;
//			midpoints[currentSegment]++;
//		}
//		for (int i = 0; i < midpoints.length; i++) {
//			int width = segmentWidth;
//			if (i == midpoints.length-1)
//				width = horizon - i*segmentWidth;
//			midpoints[i] = midpoints[i]/width;
//		}
//		return midpoints;
//	}

	@Override
	public double generateArrival(double time, int horizon) {
		double currTime = time;
		
		double lambdaUB = max(intercept);
		ExponentialDistribution exponential = new ExponentialDistribution(random, 1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		
		if (currTime < horizon) {
			int segment = (int) Math.floor(currTime/segmentWidth);
			double acceptProb = intercept[segment]/lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				if (currTime >= horizon)
					break;
				segment = (int) Math.floor(currTime/segmentWidth);
				acceptProb = intercept[segment]/lambdaUB;
				r = random.nextDouble();
			}
		}
		return currTime;
	}
	
	public List<Double> generateArrivalsFromProcess(int horizon) {
		List<Double> events = new ArrayList<>();
		double currTime = 0;
		while (currTime < horizon) {
			double r = generateArrival(currTime, horizon);
			currTime = r;
			events.add(r);
		}
		return events;
	}
	
//	public void exportDrawsFromProcess(int window, String fileName) throws IOException {
//		int nrWindows = (int) Math.ceil((double)horizon/window);
//		int[] counts = new int[nrWindows];
//		Arrays.fill(counts, 0);
//		List<Double> events = generateArrivalsFromProcess();
//		for (double v : events) {
//			if (v < horizon)
//				counts[v/window]++;
//		}
//		File file = new File(fileName);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//		for (int i = 0; i < counts.length; i++) {
//			int v = counts[i];
//			int xAxis = i*window + window/2;
//			bw.write(xAxis + "," + String.valueOf( (double)v/window ));
//			bw.newLine();
//		}
//		bw.flush();
//		bw.close();
//	}
	
//	public void exportArrivalRate(String fileName) throws IOException {
//		File file = new File(fileName);
//		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//		for (int i = 0; i < horizon; i++) {
//			bw.write(i + "," + midpoints[i/segmentWidth]);
//			bw.newLine();
//		}
//		bw.flush();
//		bw.close();
//	}
	
//	public double kolmogorovSmirnovTest(String filename) throws IOException {
//		List<Double> standardExponentials = new ArrayList<>();
//		
//		// transform arrivals into standard exponential variables
//		// at least, under null-hypothesis
//		
//		// init
//		List<List<Double>> arrivalTimes = new ArrayList<>();
//		int nrWindows = (int) Math.ceil((double)horizon/segmentWidth);
//		for (int i = 0; i < nrWindows; i++) {
//			arrivalTimes.add(new ArrayList<>());
//		}
//		
//		// unround passenger arrivals
//		List<Double> sortedArrivals = new ArrayList<>();
//		for (Passenger passenger : passengers) {
//			LocalDateTime checkInTime = passenger.getCheckInTime();
//			int intCheckInTime = checkInTime.toLocalTime().toSecondOfDay();
//			double unroundedTime = intCheckInTime + random.nextDouble();
//			sortedArrivals.add(unroundedTime);
//		}
//		Collections.sort(sortedArrivals);
//		
//		// create matrix of arrivals T_{ij}
//		for (double arrivalTime : sortedArrivals) {
//			int currentSegment = (int) Math.floor(arrivalTime/segmentWidth);
//			arrivalTimes.get(currentSegment).add(arrivalTime);
//		}
//		
//		// calculate R_{ij}
//		for (int i = 0; i < arrivalTimes.size(); i++) {
//			List<Double> innerTimes = arrivalTimes.get(i);
//			for (int j = 0; j < innerTimes.size(); j++) {
//				double r_left = innerTimes.size() + 1 - (j+1);
//				double Tij = innerTimes.get(j) % segmentWidth;
//				double r_log_num = segmentWidth - Tij;
//				double t_i_jmin1 = 0;
//				if (j-1 >= 0)
//					t_i_jmin1 = innerTimes.get(j-1) % segmentWidth;
//				double r_log_den = segmentWidth-t_i_jmin1;
//				double r = r_left * (-Math.log(r_log_num/r_log_den));
//				if (r < 0.0)
//					System.out.println("NEGATIVE: " + r);
//				standardExponentials.add(r);
//			}
//		}
//		// done calculating
//		double[] exps = new double[standardExponentials.size()];
//		for (int i = 0; i < exps.length; i++) {
//			exps[i] = standardExponentials.get(i);
//		}
//		
//		KolmogorovSmirnovTest kstest = new KolmogorovSmirnovTest();
//		double pVal = kstest.kolmogorovSmirnovTest(new ExponentialDistribution(1.0), exps);
//		
//		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
//		for (double val : exps) {
//			bw.write(String.valueOf(val));
//			bw.newLine();
//		}
//		bw.flush();
//		bw.close();
//		
//		return pVal;
//	}

}
