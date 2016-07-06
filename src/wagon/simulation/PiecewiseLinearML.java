package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.joptimizer.functions.*;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

public class PiecewiseLinearML implements ArrivalProcess {
	
//	private final int horizon = 24*60*60;
	
	private MersenneTwister random;
	private int segments;
	private int segmentWidth;
	private int beginTime;
	private int endTime;
	
	private double[] intercept;
	private double[] slope;
	private double[] w; // knots
	private List<Double> arrivals;
	
	public PiecewiseLinearML(
			Collection<Passenger> passengers, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			int seed) {
		random = new MersenneTwister(seed);
		
		this.segmentWidth = segmentWidth;
		this.beginTime = beginTime;
		this.endTime = endTime;
		
		segments = (endTime-beginTime)/segmentWidth;
		intercept = new double[segments];
		slope = new double[segments];
		w = new double[segments+1];
		arrivals = new ArrayList<>();
		
//		for (double val : passengers)
//			arrivals.add(val);
		
		// make knots
		for (int i = 0; i < segments+1; i++) {
			w[i] = beginTime + i*segmentWidth;
		}
		
		// process arrivals
		for (Passenger passenger : passengers) {
			double arrivalTime = passenger
					.getCheckInTime().toLocalTime().toSecondOfDay();
			arrivalTime += random.nextDouble();
			
			if (beginTime <= arrivalTime && arrivalTime <= endTime)
				arrivals.add(arrivalTime);
		}
		Collections.sort(arrivals);
		
		// fit the model
		fitModel();
	}

	@Override
	public double generateArrival(double time, int horizon) {
//		double[] a = {10, 0, 30};
//		double[] b = {-1, 1, -2};
//		double[] a = {10};
//		double[] b = {-1};
		
		double currTime = time;
		
//		double lambdaUB = getMaxLambda();
		double lambdaUB = 10;
		ExponentialDistribution exponential = new ExponentialDistribution(random, 1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		
		if (currTime < horizon) {
			double acceptProb = intercept[0] + slope[0]*currTime;
//			if (currTime >= 5)
//				acceptProb = a[1] + b[1]*currTime;
//			if (currTime >= 10)
//				acceptProb = a[2] + b[2]*currTime;
			acceptProb /= lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				acceptProb = intercept[0] + slope[0]*currTime;
//				if (currTime >= 5)
//					acceptProb = a[1] + b[1]*currTime;
//				if (currTime >= 10)
//					acceptProb = a[2] + b[2]*currTime;
				acceptProb /= lambdaUB;
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
	
//	private double getMaxLambda() {
//		double max = Double.NEGATIVE_INFINITY;
//		for (int i = 0; i < slope.length; i++) {
//			
//		}
//	}
	
	private void fitModel() {
		StrictlyConvexMultivariateRealFunction obj = new ObjFunction(
				arrivals, 
				beginTime, 
				endTime, 
				segmentWidth, 
				w);
		
		LinearMultivariateRealFunction[] nonNegativeConstraints = 
				constraintsNonNegative();
		
		// build the model
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(obj);
		or.setFi(nonNegativeConstraints);
		
		if (segments-1 > 0) {
			double[][] matrixA = constraintsContinuity();
			or.setA(matrixA);
			or.setB(new double[segments-1]);
		}
		or.setTolerance(1e-9);
		double[] initialPoint = new double[2*segments];
		Arrays.fill(initialPoint, 1.0);
		or.setInitialPoint(initialPoint);
		
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for ( Logger logger : loggers ) {
		    logger.setLevel(Level.OFF);
		}
		
		try {
			int returnCode = opt.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double[] solution = opt.getOptimizationResponse().getSolution();
		intercept = Arrays.copyOfRange(solution, 0, segments);
		slope = Arrays.copyOfRange(solution, segments, solution.length);
//		System.out.println(printVector(solution));
	}
	
	private LinearMultivariateRealFunction[] constraintsNonNegative() {
		LinearMultivariateRealFunction[] nonNegativeConstraints = 
				new LinearMultivariateRealFunction[segments+1];
		
		// constraints for i = 1, ..., m-1
		for (int i = 0; i < segments-1; i++) {
			double[] coeffs = new double[2*segments];
			double w_i = w[i+1];
			
			coeffs[i] = -1; // a_i
			coeffs[segments + i] = -w_i; // b_i
			nonNegativeConstraints[i] = new LinearMultivariateRealFunction(coeffs, 0);
		}
		
		// constraint on begin
		double[] coeffs = new double[2*segments];
		coeffs[0] = -1; // a_1
		nonNegativeConstraints[segments-1] = new LinearMultivariateRealFunction(coeffs, 0);
		
		// constraint on end
		coeffs = new double[2*segments];
		coeffs[segments - 1] = -1; // a_m
		coeffs[2*segments - 1] = -(arrivals.get(arrivals.size()-1)); // b_m
		nonNegativeConstraints[segments] = new LinearMultivariateRealFunction(coeffs, 0);
		
		return nonNegativeConstraints;
	}
	
	private double[][] constraintsContinuity() {
		double[][] matrix = new double[segments-1][2*segments];
		for (int i = 0; i < segments-1; i++) {
			double w_i = w[i+1];
			
			matrix[i][i] = -1; // a_i
			matrix[i][i+1] = 1; // a_{i+1}
			matrix[i][segments + i] = -w_i; // b_i
			matrix[i][segments + i+1] = w_i; // b_{i+1}
		}
//		System.out.println(printMatrix(matrix));
		return matrix;
	}
	
//	private String printVector(double[] vector) {
//		String s = "";
//		for (double val : vector)
//			s += String.format("%.3f", val) + System.lineSeparator();
//		return s;
//	}
	
//	private String printMatrix(double[][] matrix) {
//		String s = "";
//		for (int i = 0; i < matrix.length; i++) {
//			for (int j = 0; j < matrix[0].length; j++) {
//				double val = matrix[i][j];
//				s += String.format("%.3f", val) + "\t";
//			}
//			s += System.lineSeparator();
//		}
//		return s;
//	}
	
	private static class ObjFunction implements StrictlyConvexMultivariateRealFunction {
		
		private List<List<Double>> matrixArrivals;
		private int segments;
//		private int segmentWidth;
		private double[] w;
		
		public ObjFunction(
				List<Double> arrivals, 
				int beginTime, 
				int endTime, 
				int segmentWidth, 
				double[] w) {
			matrixArrivals = new ArrayList<>();
			this.segments = (endTime-beginTime)/segmentWidth;
//			this.segmentWidth = segmentWidth;
			this.w = w;
			
			for (int i = 0; i < segments; i++)
				matrixArrivals.add(new ArrayList<>());
			
			// divide arrivals into segments
			for (double val : arrivals) {
				int index = (int) Math.floor( (val-beginTime)/segmentWidth );
				matrixArrivals.get(index).add(val);
			}
			
			int min_arrivals = Integer.MAX_VALUE;
			for (int i = 0; i < matrixArrivals.size(); i++) {
				if (matrixArrivals.get(i).size() < min_arrivals)
					min_arrivals = matrixArrivals.get(i).size();
			}
			System.out.println(String.valueOf(min_arrivals));
			
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
				double w_i_1 = w[i];
				double w_i = w[i+1];
//				if (i > 0)
//					w_i_1 = w[i-1];
				
				// first term
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
//					double denom = a_i + b_i*(t_j - w_i_1);
					double denom = a_i + b_i*t_j;
					result[i] -= 1/denom;
				}
				
				// second term
				result[i] += w_i - w_i_1;
			}
			
			// calculate gradient for b_i
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				double w_i_1 = w[i];
				double w_i = w[i+1];
//				if (i > 0)
//					w_i_1 = w[i-1];
				// second term
				double term2 = 0.5*w_i*w_i - 0.5*w_i_1*w_i_1;
				result[segments + i] += term2;
				
				// first term
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
					double num = t_j;
					double denom = a_i + b_i*t_j;
					result[segments + i] -= num/denom;
				}
			}
			return result;
		}

		@Override
		public double[][] hessian(double[] x) {
			// assuming outer arrays represent columns, inner arrays rows
			double[][] result = new double[2*segments][2*segments];
			
			for (int i = 0; i < segments; i++) {
				double a_i = x[i];
				double b_i = x[segments + i];
				for (int k = 0; k < segments; k++) {
					// part a_i a_k
					if (i != k) {
						result[i][k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double term = a_i + b_i*t_j;
							double denom = term*term;
							result[i][k] += 1/denom;
						}
					}
					
					// part b_i b_k
					if (i != k) {
						result[segments + i][segments + k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double num = t_j;
							double term = a_i + b_i*num;
							double denom = term*term;
							result[segments + i][segments + k] += (num*num)/denom;
						}
					}
					
					// part a_i b_k -- symmetric
					if (i != k) {
						result[segments + i][k] = 0;
						result[i][segments + k] = 0;
					} else {
						List<Double> segmentArrivals = matrixArrivals.get(i);
						for (double t_j : segmentArrivals) {
							double num = t_j;
							double term = a_i + b_i*num;
							double denom = term*term;
							result[segments + i][k] += num/denom;
							result[i][segments + k] += num/denom;
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
				double w_i = w[i+1];
				double w_i_1 = w[i];
//				if (i > 0)
//					w_i_1 = w[i-1];
				
				// first expression
				List<Double> segmentArrivals = matrixArrivals.get(i);
				for (double t_j : segmentArrivals) {
					double term = a_i + b_i*t_j;
					result += Math.log(term);
				}
				
				// second expression
				result -= ( a_i*(w_i - w_i_1) + b_i*(0.5*w_i*w_i - 0.5*w_i_1*w_i_1) );
			}
			return -result;
		}
		
	}

}