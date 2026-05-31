package statistics;

/**
 * This class provides useful methods for collecting one dimensional data (of type double) and for computing basic statistics.
 */
public class StatCollector {

	private long numberOfObs;        // number of collected data
	private double avgOfObs;         // mean of collected data
	private double sumOfSquaredDev;  // sum of squared deviations to the mean of collected data

	/**
	 * Creates a new collector and initializes it
	 */
	public StatCollector() {
		init();
	}

	/**
	 * Initializes the collector
	 */
	public void init() {
		numberOfObs = 0L;
		avgOfObs = 0.0;
		sumOfSquaredDev = 0.0;
	}

	/**
	 * Adds a new observation to this collector.
	 *
	 * @param x observation to be added to this collector
	 */
	public void add(double x) {
		double delta = x - avgOfObs;
		numberOfObs++;
		avgOfObs += delta / numberOfObs;
		sumOfSquaredDev += delta * (x - avgOfObs);
	}

	/**
	 * Returns the number of observations added to this collector since its last initialization.
	 *
	 * @return the number of added observations since last initialization
	 */
	public long getNumberOfObs() {
		return numberOfObs;
	}

	/**
	 * Returns the average of the collected observations since its last initialization.
	 * <p>
	 * If no observations were added since last initialization, Double.NaN is returned.
	 *
	 * @return the average value of the collected observations
	 */
	public double getAverage() {
		if (numberOfObs == 0) {
			return Double.NaN;
		} else {
			return avgOfObs;
		}
	}

	/**
	 * Returns the sample variance of the collected observations since its last initialization.
	 * <p>
	 * If this collection contains less than two observations, Double.NaN is returned.
	 *
	 * @return the sample variance of the collected observations
	 */
	public double getVariance() {
		if (numberOfObs < 2) {
			return Double.NaN;
		} else {
			return sumOfSquaredDev / (numberOfObs - 1);
		}
	}

	/**
	 * Returns the sample standard deviation of the collected observations since its last initialization.
	 * <p>
	 * If this collection contains less than two observations, Double.NaN is returned.
	 *
	 * @return the sample standard deviation of the collected observations
	 */
	public double getStandardDeviation() {
		if (numberOfObs < 2) {
			return Double.NaN;
		} else {
			return Math.sqrt(getVariance());
		}
	}

	/**
	 * Computes a confidence interval with given confidence level for the mean of the collected observations
	 * and returns half of the interval width.
	 * <p>
	 * If this collection contains less than two observations, Double.NaN is returned.
	 *
	 * @param level the desired level of confidence of the C.I.
	 * @return the half-width of the C.I
	 * @throws IllegalArgumentException if level is not between 0 and 1
	 */
	public double getConfidenceIntervalHalfWidth(double level) {
		// Check argument
		if (level < 0.0 || level > 1.0) {
			throw new IllegalArgumentException("Confidence level should be between 0 and 1.");
		}

		double normalQuantile = InverseStdNormalCDF.getQuantile(0.5 + level / 2.0);

		if (numberOfObs < 2) {
			return Double.NaN;
		} else {
			return normalQuantile * getStandardDeviation() / Math.sqrt(numberOfObs);
		}

	}

}
