package wagon.simulation;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import com.joptimizer.functions.*;
import com.joptimizer.optimizers.*;

public class ScaledPiecewiseLinearProcess implements ArrivalProcess {
	
	private final double zeroTol = 1e-5;
	
	private MersenneTwister random;
	private int segments;
	private int segmentWidth;
	private double leftBorderPoint;
	private double rightBorderPoint;
	
	private double[] intercept;
	private double[] slope;
	private double[] w; // knots
	private List<Double> arrivals;
	
	public ScaledPiecewiseLinearProcess(
			Collection<Passenger> passengers, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			long seed) {
		this(passengers, beginTime, endTime, segmentWidth, Double.NaN, Double.NaN, seed);
	}
	
	public ScaledPiecewiseLinearProcess(
			Collection<Passenger> passengers, 
			int beginTime, 
			int endTime, 
			int segmentWidth, 
			double leftBorderPoint, 
			double rightBorderPoint, 
			long seed) {
		random = new MersenneTwister(seed);
		
		this.segmentWidth = segmentWidth;
		segments = (endTime-beginTime)/segmentWidth;
		intercept = new double[segments];
		slope = new double[segments];
		w = new double[segments+1];
		arrivals = new ArrayList<>();
		this.leftBorderPoint = leftBorderPoint;
		this.rightBorderPoint = rightBorderPoint;
		
		// make knots
		for (int i = 0; i < w.length; i++) {
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
		
		estimateModel();
	}
	
	private void estimateModel() {
		OptimizationRequest or = new OptimizationRequest();
		
		// objective function
		ConvexMultivariateRealFunction obj = new ObjFunction(arrivals, segmentWidth, w);
		or.setF0(obj);
		
		// continuity constraints
		double[][] A = constraintsContinuity();
		double[] vector = continuityRHVector();
		if (A.length > 0)
			or.setA(A);
		if (vector.length > 0)
			or.setB(vector);
		
		// nonnegativity constraint
		ConvexMultivariateRealFunction[] fi = constraintsNonNegativity();
		or.setFi(fi);
		
		// set initial feasible point
		double[] x0 = getFeasiblePoint();
		or.setInitialPoint(x0);
		
//		BarrierFunction barrier = new LogarithmicBarrier(fi, obj.getDim());
//		BarrierMethod opt = new BarrierMethod(barrier);
//		PrimalDualMethod opt = new PrimalDualMethod();
		JOptimizer opt = new JOptimizer();
//		or.setMaxIteration(10000);
		opt.setOptimizationRequest(or);
		try {
			int returnCode = opt.optimize();
			double[] solution = opt.getOptimizationResponse().getSolution();
			intercept = Arrays.copyOfRange(solution, 0, segments);
			slope = Arrays.copyOfRange(solution, segments, solution.length);
//			System.out.println("a = " + doubleArrayToString(intercept));
//			System.out.println("b = " + doubleArrayToString(slope));
		} catch (Exception e) {
			e.printStackTrace();
			Arrays.fill(intercept, Double.NaN);
			Arrays.fill(slope, Double.NaN);
		}
	}
	
	public double getIntercept(int i) {
		return intercept[i];
	}
	
	public double getSlope(int i) {
		return slope[i];
	}
	
	private double[] getFeasiblePoint() {
		double[] point = new double[2*segments];
//		double[] point = new double[segments];
		
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
			double[] coeffs = fitLine(0.0, y1, 1.0, y2);
			point[0] = coeffs[0];
			point[1] = coeffs[1];
		} else {
			double constant = (y1 + y2)/2;
			if (constant == 0.0)
				constant = 1.0;
			double[] coeffs1 = fitLine(0.0, y1, 1.0, constant);
			double[] coeffs2 = fitLine(0.0, constant, 1.0, y2);
			
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
	
	private double[][] constraintsContinuity() {
		int m = segments-1;
		double[][] A = null;
		if (!Double.isNaN(leftBorderPoint))
			m++;
		if (!Double.isNaN(rightBorderPoint))
			m++;
		A = new double[m][2*segments];
//		A = new double[m][segments];
		
		for (int i = 0; i < segments-1; i++) {
			// a_i
			A[i][i] = 1.0;
			
			// a_{i+1}
			A[i][i+1] = -1.0;
			
			// b_i
			A[i][segments + i] = 1.0;
		}
		
		// add additional constraint, if any
		// only left-border point specified
		if (!Double.isNaN(leftBorderPoint) && 
				Double.isNaN(rightBorderPoint))
			A[m-1][0] = 1.0;
		// only right-border point specified
		else if (Double.isNaN(leftBorderPoint) && 
				!Double.isNaN(rightBorderPoint)) {
			A[m-1][segments-1] = 1.0;
			A[m-1][segments] = 1.0;
		}
		// right- and left-border points specified
		else if (!Double.isNaN(leftBorderPoint) && 
				!Double.isNaN(rightBorderPoint)) {
			A[m-2][0] = 1.0;
			A[m-1][segments-1] = 1.0;
			A[m-1][2*segments-1] = 1.0;
		}
		
		return A;
	}
	
	private double[] continuityRHVector() {
		int m = segments-1;
		double[] vector = null;
		if (!Double.isNaN(leftBorderPoint))
			m++;
		if (!Double.isNaN(rightBorderPoint))
			m++;
		vector = new double[m];
		
		// add additional constraint, if any
		// only left-border point specified
		if (Double.isNaN(leftBorderPoint) && 
				!Double.isNaN(rightBorderPoint)) {
			vector[m-1] = rightBorderPoint;
			if (rightBorderPoint == 0.0)
				vector[m-1] = zeroTol;
		}
		// only right-border point specified
		else if (!Double.isNaN(leftBorderPoint) && 
				Double.isNaN(rightBorderPoint)) {
			vector[m-1] = leftBorderPoint;
			if (leftBorderPoint == 0.0)
				vector[m-1] = zeroTol;
		}
		// right- and left-border points specified
		else if (!Double.isNaN(leftBorderPoint) && 
				!Double.isNaN(rightBorderPoint)) {
			vector[m-2] = leftBorderPoint;
			if (leftBorderPoint == 0.0)
				vector[m-2] = zeroTol;
			vector[m-1] = rightBorderPoint;
			if (rightBorderPoint == 0.0)
				vector[m-1] = zeroTol;
		}
		
		return vector;
	}
	
	private ConvexMultivariateRealFunction[] constraintsNonNegativity() {
		ConvexMultivariateRealFunction[] constraints = new ConvexMultivariateRealFunction[segments];
		
		// a_i + b_i >= 0		i = 1, ..., m-1
		for (int i = 0; i < segments; i++) {
			double[] coeffs = new double[2*segments];
//			double[] coeffs = new double[segments];
			coeffs[i] = -1.0;
			coeffs[segments + i] = -1.0;
			ConvexMultivariateRealFunction constraint = new LinearMultivariateRealFunction(coeffs, 0.0);
			constraints[i] = constraint;
		}
		
		// a_1 >= 0
		double[] coeffs = new double[2*segments];
//		double[] coeffs = new double[segments];
		coeffs[0] = -1.0;
		ConvexMultivariateRealFunction constraint = new LinearMultivariateRealFunction(coeffs, 0.0);
		constraints[segments-1] = constraint;
		
		return constraints;
	}

	@Override
	public double generateArrival(double time, int horizon) {
		double currTime = time;
		
		double lambdaUB = getLambdaUpperBound();
		ExponentialDistribution exponential = new ExponentialDistribution(random, 1/lambdaUB);
		double randomExponential = exponential.sample();
		currTime += randomExponential;
		
		if (currTime < horizon) {
//			int currentSegment = (int) Math.floor(currTime/segmentWidth);
			double acceptProb = lambda(currTime)/lambdaUB;
			double r = random.nextDouble();
			while (r > acceptProb && currTime < horizon) {
				randomExponential = exponential.sample();
				currTime += randomExponential;

				acceptProb = lambda(currTime)/lambdaUB;
				r = random.nextDouble();
			}
		}
		return currTime;
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

	private double getLambdaUpperBound() {
		double max = Double.NEGATIVE_INFINITY;
		
		// check all segment points
		for (int i = 0; i < segments; i++) {
			double lambda = intercept[i];
			if (lambda > max)
				max = lambda;
		}
		max = Math.max(max, intercept[segments-1] + slope[segments-1]);
		
//		int n = arrivals.size();
//		
//		// check first arrival
//		max = Double.max(intercept[0] + slope[0]*arrivals.get(0), max);
//		
//		// check last arrival
//		max = Double.max(intercept[segments-1] + slope[segments-1]*arrivals.get(n-1), max);
		
		return max;
	}
	
	private static class ObjFunction implements ConvexMultivariateRealFunction {
		
		private List<List<Double>> matrixArrivals;
		private int segments;
		private double[] w;
		
		public ObjFunction(
				List<Double> arrivals, 
				int segmentWidth, 
				double[] w) {
			this.w = w;
			segments = (int) Math.ceil(w[w.length-1] - w[0])/segmentWidth;
			matrixArrivals = new ArrayList<>();
			for (int i = 0; i < segments; i++)
				matrixArrivals.add(new ArrayList<>());
			
			// divide arrivals into segments
			for (double val : arrivals) {
				int index = (int) Math.floor( (val-w[0])/segmentWidth );
				matrixArrivals.get(index).add(val);
			}
		}

		@Override
		public double value(double[] X) {
			double term1 = 0.0;
			double term2 = 0.0;
			for (int i = 0; i < segments; i++) {
				double a_i = X[i];
				double b_i = X[segments + i];
				double w_i = w[i+1];
				double w_i_1 = w[i];
				double d = w_i-w_i_1;
				
				for (double arrival : matrixArrivals.get(i)) {
					term1 += Math.log(a_i + b_i*( (arrival-w_i_1)/d ));
				}
				
				term2 += d*(a_i + 0.5*b_i);
			}
			double fX = term1 - term2;
			return -fX;
		}

		@Override
		public double[] gradient(double[] X) {
			double[] gradient = new double[getDim()];
			
			for (int i = 0; i < segments; i++) {
				double w_i = w[i+1];
				double w_i_1 = w[i];
				double d = w_i-w_i_1;
				double a_i = X[i];
				double b_i = X[segments + i];
				
				// a
				double res1 = 0.0;
				for (double arrival : matrixArrivals.get(i)) {
					double denom = a_i + b_i*( (arrival-w_i_1)/d );
//					double denom = a_i;
					res1 += 1/denom;
				}
				res1 -= d;
				gradient[i] = -res1;
				
				// b
				double res2 = 0.0;
				for (double arrival : matrixArrivals.get(i)) {
					double num = (arrival-w_i_1)/d;
					double denom = a_i + b_i*num;
					res2 += num/denom;
				}
				res2 -= 0.5*d;
				gradient[segments + i] = -res2;
			}
			return gradient;
		}

		@Override
		public double[][] hessian(double[] X) {
			double[][] hessian = new double[getDim()][getDim()];
			
			for (int i = 0; i < segments; i++) {
				for (int k = 0; k < segments; k++) {
					if (k != i)
						continue;
					double a_i = X[i];
					double b_i = X[segments + k];
					double w_i = w[i+1];
					double w_i_1 = w[i];
					double d = w_i-w_i_1;
					
					// a_i a_k
					double res1 = 0.0;
					for (double arrival : matrixArrivals.get(i)) {
						double denom = a_i + b_i*( (arrival-w_i_1)/d );
//						double denom = a_i;
						res1 -= 1/(denom*denom);
					}
					hessian[i][k] = -res1;
					
					// b_i b_k
					double res2 = 0.0;
					for (double arrival : matrixArrivals.get(i)) {
						double num = (arrival-w_i_1)/d;
						double denom = a_i + b_i*num;
						double frac = num/denom;
						res2 -= frac*frac;
					}
					hessian[segments + i][segments + k] = -res2;
					
					// a_i b_k / b_i a_k
					double res3 = 0.0;
					for (double arrival : matrixArrivals.get(i)) {
						double num = (arrival-w_i_1)/d;
						double denom = a_i + b_i*num;
						res3 -= num/(denom*denom);
					}
					hessian[segments + i][k] = -res3;
					hessian[i][segments + k] = -res3;
				}
			}
			return hessian;
		}

		@Override
		public int getDim() {
			return 2*segments;
//			return segments;
		}
		
	}
	
}
