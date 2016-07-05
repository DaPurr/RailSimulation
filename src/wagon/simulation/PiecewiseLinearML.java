package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import com.joptimizer.functions.*;
import com.joptimizer.optimizers.OptimizationRequest;
import com.joptimizer.optimizers.PrimalDualMethod;

public class PiecewiseLinearML implements ArrivalProcess {
	
//	private final int horizon = 24*60*60;
	private final int horizon = 10;
	
	private List<Passenger> passengers;
	private MersenneTwister random;
	private int segments;
	
	private double[] intercept;
	private double[] slope;
	private double[] w; // knots
	private List<Double> arrivals;
	
	public PiecewiseLinearML(Collection<Passenger> passengers, int segments, int seed) {
		random = new MersenneTwister(seed);
//		passengers = new ArrayList<>(passengers);
		this.segments = segments;
		intercept = new double[segments];
		slope = new double[segments];
		w = new double[segments];
//		arrivals = new double[passengers.size()];
		arrivals = generateArrivalsFromProcess(10);
		
		// make knots
		int segmentWidth = horizon/segments;
		for (int i = 0; i < segments; i++) {
			w[i] = (i+1)*segmentWidth;
		}
		
		// process arrivals
//		for (int i = 0; i < this.passengers.size(); i++) {
//			Passenger passenger = this.passengers.get(i);
//			double arrivalTime = passenger
//					.getCheckInTime().toLocalTime().toSecondOfDay();
//			arrivalTime += random.nextDouble();
//			arrivals[i] = arrivalTime;
//		}
//		Arrays.sort(arrivals);
		
		fitModel();
	}

	@Override
	public double generateArrival(double time, int horizon) {
		double[] a = {10, 5};
		double[] b = {-1, 1};
		
		double currTime = time;
		
		double lambdaUB = 10;
		ExponentialDistribution exponential = new ExponentialDistribution(random, 1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		
		if (currTime < horizon) {
			double acceptProb = a[0] + b[0]*currTime;
			if (currTime >= 5)
				acceptProb = a[1] + b[1]*currTime;
			acceptProb /= lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				acceptProb = a[0] + b[0]*currTime;
				if (currTime >= 5)
					acceptProb = a[1] + b[1]*currTime;
				acceptProb /= lambdaUB;
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
	
	private void fitModel() {
		ConvexMultivariateRealFunction obj = new ObjFunction(arrivals, horizon, segments, w);
		LinearMultivariateRealFunction[] nonNegativeConstraints = 
				constraintsNonNegative();
		double[][] matrixA = constraintsContinuity();
//		LinearMultivariateRealFunction[] allConstraints = 
//				constraintConcat(nonNegativeConstraints, continuityConstraints);
		// build the model
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(obj);
		or.setFi(nonNegativeConstraints);
		or.setA(matrixA);
		or.setB(new double[segments-1]);
		or.setTolerance(1e-9);
		
		PrimalDualMethod opt = new PrimalDualMethod();
		opt.setOptimizationRequest(or);
		try {
			int returnCode = opt.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double[] solution = opt.getOptimizationResponse().getSolution();
		intercept = Arrays.copyOfRange(solution, 0, segments);
		slope = Arrays.copyOfRange(solution, segments, solution.length);
	}
	
//	private LinearMultivariateRealFunction[] constraintConcat(
//			LinearMultivariateRealFunction[] a, 
//			LinearMultivariateRealFunction[] b) {
//		LinearMultivariateRealFunction[] result = 
//				new LinearMultivariateRealFunction[a.length + b.length];
//		for (int i = 0; i < a.length; i++)
//			result[i] = a[i];
//		for (int i = 0; i < b.length; i++)
//			result[a.length + i] = b[i];
//		return result;
//	}
	
	private LinearMultivariateRealFunction[] constraintsNonNegative() {
		LinearMultivariateRealFunction[] nonNegativeConstraints = 
				new LinearMultivariateRealFunction[segments+1];
		for (int i = 0; i < segments; i++) {
			double[] coeffs = new double[2*segments];
			coeffs[i] = -1;
			nonNegativeConstraints[i] = new LinearMultivariateRealFunction(coeffs, 0);
		}
		
		// the lone nonnegative constraint
		double[] coeffs = new double[2*segments];
		coeffs[segments - 1] = -1; // a_m
//		coeffs[2*segments - 1] = -(arrivals[arrivals.length-1] - w[segments-1]); // b_m
		coeffs[2*segments - 1] = -(arrivals.get(arrivals.size()-1) - w[segments-1]); // b_m
		nonNegativeConstraints[segments] = new LinearMultivariateRealFunction(coeffs, 0);
		
		return nonNegativeConstraints;
	}
	
	private double[][] constraintsContinuity() {
		double[][] matrix = new double[segments-1][2*segments];
		for (int i = 0; i < segments-1; i++) {
			double w_i = w[i];
			double w_i_1 = 0.0;
			if (i > 0)
				w_i_1 = w[i-1];
			
			matrix[i][i] = 1; // a_i
			matrix[i][segments + i] = w_i - w_i_1; // b_i
			matrix[i][i+1] = -1; // a_{i+1}
		}
		return matrix;
	}
	
	private static class ObjFunction implements ConvexMultivariateRealFunction {
		
		private List<List<Double>> matrixArrivals;
		private int segments;
		private int segmentWidth;
		private double[] w;
		
		public ObjFunction(List<Double> arrivals, int horizon, int segments, double[] w) {
			matrixArrivals = new ArrayList<>();
			this.segments = segments;
			this.w = w;
			segmentWidth = horizon/segments;
			
			for (int i = 0; i < segments; i++)
				matrixArrivals.add(new ArrayList<>());
			
			// divide arrivals into segments
			for (double val : arrivals) {
				int index = (int) Math.floor(val/segmentWidth);
				matrixArrivals.get(index).add(val);
			}
			
		}

		@Override
		public int getDim() {
			return segments*2;
		}

		@Override
		public double[] gradient(double[] x) {
			double[] result = new double[2*segments];
			 
			// calculate gradient part of a_i
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				double w_i_1 = 0.0;
				if (i > 0)
					w_i_1 = w[i-1];
				
				// first term
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
					double denom = a_i + b_i*(t_j - w_i_1);
					result[i] += 1/denom;
				}
				
				// second term
				result[i] += -(w[i] - w_i_1);
			}
			
			// calculate gradient for b_i
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				double w_i_1 = 0.0;
				double w_i = w[i];
				if (i > 0)
					w_i_1 = w[i-1];
				// second term
				double term2 = 0.5*w_i*w_i + 0.5*w_i_1*w_i_1 - w_i*w_i_1;
				result[segments + i] += term2;
				
				// first term
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
					double num = t_j - w_i_1;
					double denom = a_i + b_i*(t_j - w_i_1);
					result[segments + i] += num/denom;
				}
			}
			return result;
		}

		@Override
		public double[][] hessian(double[] x) {
			// assuming outer arrays represent columns, inner arrays rows
			double[][] result = new double[segments][segments];
			
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				double w_i_1 = 0.0;
				if (i > 0)
					w_i_1 = w[i-1];
				for (int k = 0; k < segments; k++) {
					// part a_i a_k
					if (i != k) {
						result[i][k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double term = a_i + b_i*(t_j-w_i_1);
							double denom = term*term;
							result[i][k] -= 1/denom;
						}
					}
					
					// part b_i b_k
					if (i != k) {
						result[segments + i][segments + k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double num = t_j-w_i_1;
							double term = a_i + b_i*num;
							double denom = term*term;
							result[i][k] -= (num*num)/denom;
						}
					}
					
					// part a_i b_k -- symmetric
					if (i != k) {
						result[segments + i][k] = 0;
						result[i][segments + k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double num = t_j-w_i_1;
							double term = a_i + b_i*num;
							double denom = term*term;
							result[segments + i][k] -= num/denom;
							result[i][segments + k] -= num/denom;
						}
					}
				}
			}
			return result;
		}

		@Override
		public double value(double[] x) {
			double result = 0.0;
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				double w_i = w[i];
				double w_i_1 = 0.0;
				if (i > 0)
					w_i_1 = w[i-1];
				
				// first expression
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
					double term = a_i + b_i*(t_j - w_i_1);
					result += Math.log(term);
				}
				
				// second expression
				result -= ( a_i*(w_i - w_i_1) + b_i*(0.5*w_i*w_i + 0.5*w_i_1*w_i_1 - w_i*w_i_1) );
			}
			return result;
		}
		
	}

}
