package montecarlo;

import statistics.InverseStdNormalCDF;
import statistics.StatCollector;

import java.util.Random;

/** This class provides methods for simple Monte Carlo simulations. */
public class MonteCarloSimulation {
  /** Private constructor. Makes it impossible to instantiate. */
  private MonteCarloSimulation() {}

  /**
   * Simulates experiment exp n times, using rnd as a source of pseudo-random numbers and collect
   * the results in stat.
   *
   * @param exp experiment to be run each time
   * @param n number of runs to be performed
   * @param rnd random source to be used to simulate the experiment
   * @param stat collector to be used to collect the results of each experiment
   */
  public static void simulateNRuns(Experiment exp, long n, Random rnd, StatCollector stat) {
    for (long run = 0; run < n; ++run) {
      stat.add(exp.execute(rnd));
    }
  }

  /**
   * First simulates experiment exp initialNumberOfRuns times, then estimates the number of runs
   * needed for a 95% confidence interval half width no more than maxHalfWidth. If final C.I. is too
   * wide, simulates additionalNumberOfRuns before recalculating the C.I. and repeats the process as
   * many times as needed.
   *
   * <p>Uses rnd as a source of pseudo-random numbers and collects the results in stat.
   *
   * @param exp experiment to be run each time
   * @param level confidence level of the confidence interval
   * @param maxHalfWidth maximal half width of the confidence interval
   * @param initialNumberOfRuns initial number of runs to be performed
   * @param additionalNumberOfRuns additional number of runs to be performed if C.I. is too wide
   * @param rnd random source to be used to simulate the experiment
   * @param stat collector to be used to collect the results of each experiment
   */
  public static void simulateTillGivenCIHalfWidth(
      Experiment exp,
      double level,
      double maxHalfWidth,
      long initialNumberOfRuns,
      long additionalNumberOfRuns,
      Random rnd,
      StatCollector stat) {

    System.out.println("Calling simulation for : " + initialNumberOfRuns);
    simulateNRuns(exp, initialNumberOfRuns, rnd, stat);

    // TODO Afficher au minimum 6 décimales
    double currentHalfWidth = stat.getConfidenceIntervalHalfWidth(level);
    long totalRuns = initialNumberOfRuns;

    double theoricNumberOfRuns = totalRuns * Math.pow(currentHalfWidth / maxHalfWidth, 2);

    // Round up to nearest multiple of additionalNumberOfRuns
    long roundedNumberOfRuns =
        (long) (Math.ceil(theoricNumberOfRuns / additionalNumberOfRuns) * additionalNumberOfRuns);
    long actualNumberOfRuns = roundedNumberOfRuns - initialNumberOfRuns;

    if (actualNumberOfRuns > 0) {
      simulateNRuns(exp, actualNumberOfRuns, rnd, stat);
      totalRuns += actualNumberOfRuns;

      while (true) {
        currentHalfWidth = stat.getConfidenceIntervalHalfWidth(level);

        if (currentHalfWidth <= maxHalfWidth) {
          return;
        }

        System.out.println("Calling simulation for : " + additionalNumberOfRuns);
        simulateNRuns(exp, additionalNumberOfRuns, rnd, stat);
        totalRuns += additionalNumberOfRuns;
      }
    }
  }

  public static double simulatePerformancesIntegrationMethods(
      Experiment exp, double level, double maxHalfWidth, StatCollector stat, Random rnd) {

    long numberOfRuns = 500_000_000;

    simulateNRuns(exp, numberOfRuns, rnd, stat);

    double currentHalfWidth = stat.getConfidenceIntervalHalfWidth(level);

    double theoricNumberOfRuns = numberOfRuns * Math.pow(currentHalfWidth / maxHalfWidth, 2);

    // Round up to nearest multiple of additionalNumberOfRuns
    long roundedNumberOfRuns = (long) (Math.ceil(theoricNumberOfRuns / 500_000_000) * 500_000_000);
    long actualNumberOfRuns = roundedNumberOfRuns - numberOfRuns;

    return theoricNumberOfRuns;
  }
}
