package test.simulation;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.junit.Test;

public class PiecewiseConstantProcessTest {
	
	private MersenneTwister random = new MersenneTwister(0);
	private final int horizon = 24*60*60;

	@Test
	public void test() {
		double lambda = 1.0/3;
		System.out.println(expectationConstant(lambda));
	}
	
	private double generateArrival(double time, double lambda) {
		ExponentialDistribution expDist = new ExponentialDistribution(random, 1/lambda);
		double timeUntilNextEvent = expDist.sample();
		int roundedTime = (int) Math.ceil(time + timeUntilNextEvent);
		return roundedTime;
//		return time + timeUntilNextEvent;
	}
	
	private double expectationConstant(double lambda) {
		List<Integer> countArrivals = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			int numberOfArrivals = 0;
			double time = generateArrival(0, lambda);
			while (time < horizon) {
				numberOfArrivals++;
				time = generateArrival(time, lambda);
			}
			countArrivals.add(numberOfArrivals);
		}
		return mean(countArrivals);
	}
	
	private double mean(Collection<Integer> values) {
		double sum = 0.0;
		for (int v : values)
			sum += v;
		return sum/values.size();
	}

}
