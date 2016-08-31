package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;

public class HybridArrivalProcess implements ArrivalProcess {
	
	private List<Passenger> passengers;
	private int segmentWidth;
	private int segments;
	private double[] w; // knots
	
	private double[] intercept;
	private double[] slope;
	
	private MersenneTwister random;
	
	public HybridArrivalProcess(
			Collection<Passenger> passengers, 
			int leftBound, 
			int rightBound, 
			int segmentWidth, 
			long seed) {
		this.segmentWidth = segmentWidth;
		this.segments = (rightBound-leftBound)/segmentWidth;
		this.random = new MersenneTwister(seed);
		this.passengers = new ArrayList<>(passengers);
		
		this.intercept = new double[segments];
		this.slope = new double[segments];
		
		// make knots
		w = new double[segments+1];
		for (int i = 0; i < w.length; i++) {
			w[i] = leftBound + i*segmentWidth;
		}
		
		// convert collection of passengers to arrivals
//		for (Passenger passenger : passengers) {
//			double arrivalTime = passenger
//					.getCheckInTime().toLocalTime().toSecondOfDay();
//			arrivalTime += random.nextDouble();
//			arrivals.add(arrivalTime);
//		}
//		Collections.sort(arrivals);
		
		estimateModel();
		
		// CiCo correction
//		double k = 1.215767122505321;
//		for (int i = 0; i < segments; i++) {
//			intercept[i] *= k;
//			slope[i] *= k;
//		}
	}
	
	public double logLikelihood() {
		List<List<Double>> matrixArrivals = new ArrayList<>();
		
		for (int i = 0; i < segments; i++)
			matrixArrivals.add(new ArrayList<>());
		
		// divide arrivals into segments
		for (Passenger passenger : passengers) {
			double val = passenger.getCheckInTime().toLocalTime().toSecondOfDay();
			int index = (int) Math.floor(val/segmentWidth);
			matrixArrivals.get(index).add(val);
		}
		
		int min_arrivals = Integer.MAX_VALUE;
		for (int i = 0; i < matrixArrivals.size(); i++) {
			if (matrixArrivals.get(i).size() < min_arrivals)
				min_arrivals = matrixArrivals.get(i).size();
		}
		
		double term1 = 0.0;
		double term2 = 0.0;
		for (int i = 0; i < segments; i++) {
			double a_i = intercept[i];
			double b_i = slope[i];
			double w_i = w[i+1];
			double w_i_1 = w[i];
			double d = w_i-w_i_1;
			
			for (double arrival : matrixArrivals.get(i)) {
				term1 += Math.log(a_i + b_i*( (arrival-w_i_1)/d ));
			}
			
			term2 += d*(a_i + 0.5*b_i);
		}
		double fX = term1 - term2;
		return fX;
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
			
			// 1 segment
			if (rightBound-leftBound == 1) {
				if (leftBound > 0 && rightBound < segments-1) {

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
		}
	}
	
	public List<Double> getIntercept() {
		List<Double> list = new ArrayList<>();
		for (double d : intercept) {
			list.add(d);
		}
		return list;
	}
	
	public List<Double> getSlope() {
		List<Double> list = new ArrayList<>();
		for (double d : slope) {
			list.add(d);
		}
		return list;
	}
	
	private void fitLinearProcess(
			Collection<Passenger> passengers, 
			List<List<Passenger>> arrivalsPerSegment, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			double leftBorderPoint, 
			double rightBorderPoint) {
		ScaledPiecewiseLinearProcess process = new ScaledPiecewiseLinearProcess(
				passengers, 
				beginTime, 
				endTime, 
				segmentWidth, 
				leftBorderPoint, 
				rightBorderPoint, 
				random.nextLong());
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
			double acceptProb = lambda(currTime)/lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				if (currTime >= horizon)
					break;
				acceptProb = lambda(currTime)/lambdaUB;
				r = random.nextDouble();
			}
		}
		return currTime;
	}

	@Override
	public List<Double> generateArrivalsFromProcess(int horizon) {
		List<Double> events = new ArrayList<>();
		double currTime = 0;
		while (currTime < horizon) {
			double r = generateArrival(currTime, horizon);
			currTime = r;
			if (r < horizon)
				events.add(r);
		}
		return events;
	}
	
	public double getLambdaUpperBound() {
		double max = Double.NEGATIVE_INFINITY;
		
		// check all segment points
		for (int i = 0; i < segments; i++) {
			double lambda = intercept[i];
			if (lambda > max)
				max = lambda;
		}
		
		// check for last boundary
		max = Double.max(max, intercept[segments-1] + slope[segments-1]);
		
		return max;
	}
	
	private double lambda(double t) {
		int segment = (int) Math.floor(t/segmentWidth);
		double a_i = intercept[segment];
		double b_i = slope[segment];
		double w_i = w[segment+1];
		double w_i_1 = w[segment];
		double d = w_i-w_i_1;
		double s = (t-w_i_1)/d;
		
		double lambda_t = a_i + b_i*s;
		return lambda_t;
	}

}
