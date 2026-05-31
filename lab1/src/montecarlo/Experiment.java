package montecarlo;

import java.util.Random;

/**
 * Classes to be used for Monte Carlo simulations should implement this interface.
 */
public interface Experiment {

	/**
	 * Simulates the experiment once, using rnd as a source of pseudo-random numbers.
	 *
	 * @param rnd random source to be used to simulate the experiment
	 * @return realization of the performance measure for the experiment (of type double)
	 */
	double execute(Random rnd);
}
