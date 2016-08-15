package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.joptimizer.functions.*;
import com.joptimizer.optimizers.BarrierMethod;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

public class PiecewiseLinearProcess implements ArrivalProcess {
	
//	private final int horizon = 24*60*60;
	
	private final double zeroTol = 1e-5;
	
	private MersenneTwister random;
	private int segments;
	private int segmentWidth;
	private int beginTime;
	private int endTime;
	private double leftBorderPoint;
	private double rightBorderPoint;
	
	private double[] intercept;
	private double[] slope;
	private double[] w; // knots
	private List<Double> arrivals;
	
	public PiecewiseLinearProcess(
			Collection<Passenger> passengers, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			long seed) {
		this(
				passengers, 
				beginTime, 
				endTime, 
				segmentWidth, 
				Double.NaN, 
				Double.NaN, 
				seed);
	}
	
	public PiecewiseLinearProcess(
			Collection<Passenger> passengers, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			double leftBorderPoint, 
			double rightBorderPoint, 
			long seed) {
		random = new MersenneTwister(seed);
		
		this.segmentWidth = segmentWidth;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.leftBorderPoint = leftBorderPoint;
		this.rightBorderPoint = rightBorderPoint;
		
		segments = (endTime-beginTime)/segmentWidth;
		intercept = new double[segments];
		slope = new double[segments];
		w = new double[segments+1];
		arrivals = new ArrayList<>();
		
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
//		for (int i = 0; i < 253; i++)
//			arrivals.add(7*60*60 + random.nextDouble());
		Collections.sort(arrivals);
		
		// fit the model
		fitModel();
	}

	@Override
	@Deprecated
	public double generateArrival(double time, int horizon) {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	@Deprecated
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
	
	public double getIntercept(int segment) {
		if (segment < 0 || segment >= segments)
			throw new IllegalArgumentException("Segment number is not valid.");
		return intercept[segment];
	}
	
	public double getSlope(int segment) {
		if (segment < 0 || segment >= segments)
			throw new IllegalArgumentException("Segment number is not valid.");
		return slope[segment];
	}
	
	private double getLambdaUpperBound() {
		double max = Double.NEGATIVE_INFINITY;
		
		// check all segment points
		for (int i = 0; i < segments; i++) {
			double lambda = intercept[i] + w[i]*slope[i];
			if (lambda > max)
				max = lambda;
		}
		
		int n = arrivals.size();
		
		// check first arrival
		max = Double.max(intercept[0] + slope[0]*arrivals.get(0), max);
		
		// check last arrival
		max = Double.max(intercept[segments-1] + slope[segments-1]*arrivals.get(n-1), max);
		
		return max;
	}
	
	private void fitModel() {
		ConvexMultivariateRealFunction obj = new ObjFunction(
				arrivals, 
				beginTime, 
				endTime, 
				segmentWidth, 
				w);
		
		LinearMultivariateRealFunction[] nonNegativeConstraints = 
				constraintsNonNegativity();
		
		// build the model
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(obj);
		or.setFi(nonNegativeConstraints);
		
		double[][] matrixA = constraintsContinuity();
		if (matrixA.length > 0)
			or.setA(matrixA);
			
		double[] vectorB = continuityRHVector();
		if (vectorB.length > 0)
			or.setB(vectorB);
		
//		or.setTolerance(1e-9);
		double[] initialPoint = getFeasiblePoint();
		or.setInitialPoint(initialPoint);
		
		BarrierFunction barrier = new LogarithmicBarrier(nonNegativeConstraints, 2*segments);
		BarrierMethod opt = new BarrierMethod(barrier);
//		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(or);
		
//		BasicConfigurator.configure();
//		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
//		loggers.add(LogManager.getRootLogger());
//		for ( Logger logger : loggers ) {
//		    logger.setLevel(Level.OFF);
//		}
		
		try {
			int returnCode = opt.optimize();
			double[] solution = opt.getOptimizationResponse().getSolution();
			intercept = Arrays.copyOfRange(solution, 0, segments);
			slope = Arrays.copyOfRange(solution, segments, solution.length);
		} catch (Exception e) {
			e.printStackTrace();
			Arrays.fill(intercept, Double.NaN);
			Arrays.fill(slope, Double.NaN);
		}
//		System.out.println(printVector(solution));
	}
	
	private double[] getFeasiblePoint() {
		double[] point = new double[2*segments];
		
		double y1 = 1.0;
		double y2 = 1.0;
		
		if (!Double.isNaN(leftBorderPoint)) {
			y1 = leftBorderPoint;
			if (y1 == 0)
				y1 = zeroTol;
		}
		if (!Double.isNaN(rightBorderPoint)) {
			y2 = rightBorderPoint;
			if (y2 == 0)
				y2 = zeroTol;
		}
		
		if (segments == 1) {
			double[] coeffs = fitLine(w[0], y1, w[1], y2);
			point[0] = coeffs[0];
			point[1] = coeffs[1];
		} else {
			double constant = (y1 + y2)/2;
			if (constant == 0)
				constant = 1;
			double[] coeffs1 = fitLine(w[0], y1, w[1], constant);
			double[] coeffs2 = fitLine(w[w.length-2], constant, w[w.length-1], y2);
			
			// first segment possibly linear
			point[0] = coeffs1[0];
			point[segments] = coeffs1[1];
			
			// last segment possibly linear
			point[segments-1] = coeffs2[0];
			point[2*segments-1] = coeffs2[1];
			
			// all in between are constant
			for (int i = 1; i < segments-1; i++)
				point[i] = constant;
		}
		
		return point;
	}
	
	private double[] fitLine(double x1, double y1, double x2, double y2) {
		double b = (y2-y1)/(x2-x1);
		double a = y1 - x1*b;
		return new double[] {a, b};
	}
	
	private LinearMultivariateRealFunction[] constraintsNonNegativity() {
		LinearMultivariateRealFunction[] nonNegativityConstraints = 
				new LinearMultivariateRealFunction[segments+1];
		
		// constraints for i = 1, ..., m-1
		for (int i = 0; i < segments-1; i++) {
			double[] coeffs = new double[2*segments];
			double w_i = w[i+1];
			
			coeffs[i] = -1; // a_i
			coeffs[segments + i] = -w_i; // b_i
			nonNegativityConstraints[i] = new LinearMultivariateRealFunction(coeffs, 0);
		}
		
		// constraint on begin
		double[] coeffs = new double[2*segments];
		coeffs[0] = -1; // a_1
		coeffs[segments] = -w[0];
		nonNegativityConstraints[segments-1] = new LinearMultivariateRealFunction(coeffs, 0);
		
		// constraint on end
		coeffs = new double[2*segments];
		coeffs[segments - 1] = -1; // a_m
//		coeffs[2*segments - 1] = -(arrivals.get(arrivals.size()-1)); // b_m
		coeffs[2*segments - 1] = -w[w.length-1];
		nonNegativityConstraints[segments] = new LinearMultivariateRealFunction(coeffs, 0);
		
		return nonNegativityConstraints;
	}
	
	private double[][] constraintsContinuity() {
		
		List<List<Double>> matrix = new ArrayList<>(segments-1 + 2);
		for (int i = 0; i < segments-1; i++) {
			ArrayList<Double> list = new ArrayList<>();
			appendList(list, 0.0, 2*segments);
			matrix.add(list);
		}
		
//		double[][] matrix = new double[segments-1 + 2][2*segments];
		for (int i = 0; i < segments-1; i++) {
			double w_i = w[i+1];
			List<Double> row = matrix.get(i);
			row.set(i, -1.0); // a_i
			row.set(i+1, 1.0); // a_{i+1}
			row.set(segments+i, -w_i); // b_i
			row.set(segments + i+1, w_i); // b_{i+1}
		}
		
		// left border constraint
		if (!Double.isNaN(leftBorderPoint)) {
			double w_0 = w[0];
			
			ArrayList<Double> list = new ArrayList<>();
			appendList(list, 0.0, 2*segments);
			list.set(0, 1.0);
			list.set(segments, w_0);
			
			matrix.add(list);
		}
		
		// right border constraint
		if (!Double.isNaN(rightBorderPoint)) {
			double w_m = w[segments];

			ArrayList<Double> list = new ArrayList<>();
			appendList(list, 0.0, 2*segments);
			list.set(segments-1, 1.0);
			list.set(2*segments-1, w_m);
			
			matrix.add(list);
		}
		
//		System.out.println(printMatrix(matrix));
		if (matrix.size() == 0)
			return new double[0][0];
		double[][] arrayMatrix = new double[matrix.size()][matrix.get(0).size()];
		for (int i = 0; i < arrayMatrix.length; i++) {
			for (int j = 0; j < arrayMatrix[0].length; j++) {
				arrayMatrix[i][j] = matrix.get(i).get(j);
			}
		}
		return arrayMatrix;
	}
	
	private <T> void appendList(List<T> list, T obj, int count) {
		for (int i = 0; i < count; i++)
			list.add(obj);
	}
	
	private double[] continuityRHVector() {
		List<Double> vector = new ArrayList<>(segments-1 + 2);
		appendList(vector, 0.0, segments-1);
		
		if (!Double.isNaN(leftBorderPoint)) {
			if (leftBorderPoint == 0)
				vector.add(zeroTol);
			else
				vector.add(leftBorderPoint);
		}
		
		if (!Double.isNaN(rightBorderPoint)) {
			if (rightBorderPoint == 0)
				vector.add(zeroTol);
			else
				vector.add(rightBorderPoint);
		}
		
		double[] arrayVector = new double[vector.size()];
		for (int i = 0; i < vector.size(); i++)
			arrayVector[i] = vector.get(i);
		
		return arrayVector;
	}
	
	private static class ObjFunction implements ConvexMultivariateRealFunction {
		
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
//			System.out.println(String.valueOf(min_arrivals));
			
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

		// TODO: CHECK FOR SUMMATION!!
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
