package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.poi.hssf.record.RightMarginRecord;

public class HybridArrivalProcess implements ArrivalProcess {
	
//	private List<Double> arrivals;
	private List<Passenger> passengers;
	private int segmentWidth;
	private int segments;
	
	private double[] intercept;
	private double[] slope;
	
	private MersenneTwister random;
	private int seed = 0;
	
	public HybridArrivalProcess(
			Collection<Passenger> passengers, 
			int leftBound, 
			int rightBound, 
			int segmentWidth, 
			int seed) {
		this.segmentWidth = segmentWidth;
		this.segments = (rightBound-leftBound)/segmentWidth;
//		this.arrivals = new ArrayList<>();
		this.random = new MersenneTwister(seed);
		this.passengers = new ArrayList<>(passengers);
		
		this.intercept = new double[segments];
		this.slope = new double[segments];
		
		// convert collection of passengers to arrivals
//		for (Passenger passenger : passengers) {
//			double arrivalTime = passenger
//					.getCheckInTime().toLocalTime().toSecondOfDay();
//			arrivalTime += random.nextDouble();
//			arrivals.add(arrivalTime);
//		}
//		Collections.sort(arrivals);
		
		estimateModel();
	}
	
	private void estimateModel() {
		List<Integer> segmentsWithConstantRate = new ArrayList<>();
		List<List<Passenger>> arrivalsPerSegment = new ArrayList<>();
		for (int i = 0; i < segments; i++)
			arrivalsPerSegment.add(new ArrayList<>());
		
		// calculate number of arrivals per segment
		for (Passenger passenger : passengers) {
			double arrivalTime = passenger.getCheckInTime().toLocalTime().toSecondOfDay();
			int segment = (int) Math.floor(arrivalTime/segmentWidth);
			arrivalsPerSegment.get(segment).add(passenger);
		}
		
		// estimate piecewise-constant arrival rate for every segment with 0/1 arrival
		for (int i = 0; i < arrivalsPerSegment.size(); i++) {
			int freq = arrivalsPerSegment.get(i).size();
			if (freq == 0) {
				intercept[i] = 0;
				slope[i] = 0;
				segmentsWithConstantRate.add(i);
			} else if (freq == 1) {
				intercept[i] = 1.0/segmentWidth;
				slope[i] = 0;
				segmentsWithConstantRate.add(i);
			}
		}
		
		// for every consecutive sequence of segments without constant arrival rate, 
		// estimate a continuous piecewise-linear rate
		int leftBound = 0;
		int rightBound = 0;
		for (int constantSegment : segmentsWithConstantRate) {
			rightBound = constantSegment;
			
			// 1 segment
			if (rightBound-leftBound == 1) {
				if (leftBound > 0 && rightBound < segments-1) {
//					// there is only one line
//					int w_0 = leftBound*segmentWidth;
//					int w_1 = rightBound*segmentWidth;
//					double leftPoint = intercept[leftBound-1] + slope[leftBound-1]*w_0;
//					double rightPoint = intercept[rightBound+1] + slope[rightBound+1]*w_1;
//					
//					// 'fit' model
//					slope[leftBound] = (rightPoint-leftPoint)/(w_1-w_0);
//					intercept[leftBound] = leftPoint - w_0*slope[leftBound];
					
					// constant process
					slope[leftBound] = 0;
					intercept[leftBound] = (double) arrivalsPerSegment.get(leftBound).size()/segmentWidth;
				} else {
					// at least one side is free, so fit piecewise-linear model
					double leftPoint = Double.NaN;
					double rightPoint = Double.NaN;
					if (leftBound == 0) {
						rightPoint = intercept[rightBound+1];
					} else {
						leftPoint = intercept[leftBound-1];
					}
					fitLinearProcess(
							arrivalsPerSegment.get(leftBound), 
							arrivalsPerSegment, 
							leftBound*segmentWidth, 
							rightBound*segmentWidth, 
							segmentWidth, 
							leftPoint, 
							rightPoint);
				}
			}
			// >1 segment
			else if (rightBound-leftBound > 1) {
				// retrieve passengers
				List<Passenger> selectedPassengers = new ArrayList<>();
				for (int i = leftBound; i < rightBound; i++) {
					selectedPassengers.addAll(arrivalsPerSegment.get(i));
				}
				
				double leftPoint = Double.NaN;
				double rightPoint = Double.NaN;
				
				if (leftBound > 0) {
					leftPoint = intercept[leftBound-1] + slope[leftBound-1]*(leftBound*segmentWidth);
				}
				if (rightBound < segments-1) {
					rightPoint = intercept[rightBound+1] + slope[rightBound+1]*(rightBound*segmentWidth);
				}
				
				fitLinearProcess(
						selectedPassengers, 
						arrivalsPerSegment, 
						leftBound*segmentWidth, 
						rightBound*segmentWidth, 
						segmentWidth, 
						leftPoint, 
						rightPoint);
			}
			
			leftBound = rightBound+1;
		}
		rightBound = segments-1;
		if (rightBound-leftBound > 0) {
			// fit a process
		}
	}
	
	private void fitLinearProcess(
			Collection<Passenger> passengers, 
			List<List<Passenger>> arrivalsPerSegment, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			double leftBorderPoint, 
			double rightBorderPoint) {
		PiecewiseLinearProcess process = new PiecewiseLinearProcess(
				passengers, 
				beginTime, 
				endTime, 
				segmentWidth, 
				0);
		// get parameters
		int nrSegments = (endTime-beginTime)/segmentWidth;
		for (int i = 0; i < nrSegments; i++) {
			double intercept = process.getIntercept(i);
			double slope = process.getSlope(i);
			
			int correspondingSegment = beginTime/segmentWidth + i;
			
			if (Double.isNaN(intercept)) {
				this.slope[correspondingSegment] = 0;
				this.intercept[correspondingSegment] = (double) arrivalsPerSegment.get(correspondingSegment).size()/segmentWidth;
			} else {
				this.intercept[correspondingSegment] = intercept;
				this.slope[correspondingSegment] = slope;
			}
		}
	}

	@Override
	public double generateArrival(double time, int horizon) {
		double currTime = time;
		
		double lambdaUB = getLambdaUpperBound();
		ExponentialDistribution exponential = new ExponentialDistribution(random, 1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		
		if (currTime < horizon) {
			int currentSegment = (int) Math.floor(currTime/segmentWidth);
			double acceptProb = intercept[currentSegment] + slope[currentSegment]*currTime;
			acceptProb /= lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				acceptProb = intercept[currentSegment] + slope[currentSegment]*currTime;
				acceptProb /= lambdaUB;
				r = random.nextDouble();
			}
		}
		return currTime;
	}

	@Override
	public List<Double> generateArrivalsFromProcess(int horizon) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private double getLambdaUpperBound() {
		double max = Double.NEGATIVE_INFINITY;
		
		// check all segment points
		for (int i = 0; i < segments; i++) {
			int w_i = i*segmentWidth;
			double lambda = intercept[i] + w_i*slope[i];
			if (lambda > max)
				max = lambda;
		}
		
		// check for last boundary
		max = Double.max(max, intercept[segments-1] + slope[segments-1]*(segments*segmentWidth));
		
//		int n = passengers.size();
//		
//		// check first arrival
//		max = Double.max(intercept[0] + slope[0]*arrivals.get(0), max);
//		
//		// check last arrival
//		max = Double.max(intercept[segments-1] + slope[segments-1]*arrivals.get(n-1), max);
		
		return max;
	}

}
