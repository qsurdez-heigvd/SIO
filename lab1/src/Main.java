import montecarlo.*;
import statistics.*;

import java.util.Random;

// Juste pour l'exemple
class FairCoinTossExperiment implements Experiment {
  public double execute(Random rnd) {
    return rnd.nextDouble() < 0.5 ? 1.0 : 0.0;
  }
}

class PlayerRuin implements Experiment {

  int initialFortune;
  double probability;

  PlayerRuin(int initialFortune, double probability) {
    this.initialFortune = initialFortune;
    this.probability = probability;
  }

  @Override
  public double execute(Random rnd) {
    int currentFortune = initialFortune;

    while (currentFortune > 0 && currentFortune < initialFortune * 2) {
      currentFortune += rnd.nextDouble() < probability ? 1 : -1;
    }

    return currentFortune >= initialFortune * 2 ? 1.0 : 0.0;
  }
}

public class Main {

  public static void main(String[] args) {
    // Juste pour l'exemple et vérifier que tout compile
    /*    StatCollector stat = new StatCollector();

    Random rnd = new Random();
    rnd.setSeed(0x1350185);

    Experiment exp = new PlayerRuin(5, 18.0 / 37.0);

    MonteCarloSimulation.simulateTillGivenCIHalfWidth(exp, 0.95, 10e-4, 1_000_000, 100_000, rnd, stat);

    System.out.printf("**********************%n  Simulation results%n**********************%n");
    System.out.printf("Number of games generated: %d%n", stat.getNumberOfObs());
    System.out.printf("Estimated prob. of doubling:    %.5f%n", stat.getAverage());
    System.out.printf(
        "Confidence interval (95%%):  %.5f +/- %.5f%n",
        stat.getAverage(), stat.getConfidenceIntervalHalfWidth(0.95));

    */
    Random rnd = new Random();
    rnd.setSeed(0x1350185);

    StatCollector stat = new StatCollector();

    Experiment exp = new AcceptanceRejection(0.0, 6.0, 2.0);

    double numberOfRunsNeeded =
        MonteCarloSimulation.simulatePerformancesIntegrationMethods(exp, 0.95, 10e-5, stat, rnd);
    System.out.println("=== AcceptanceRejection ===");
    System.out.println("Number of runs needed: " + numberOfRunsNeeded);
    System.out.println("Current estimator of G: " + stat.getAverage());

    stat.init();

    Experiment exp1 = new UniformSamplingMethod(0, 6);

    double numberOfRunsNeededUniformSampling =
        MonteCarloSimulation.simulatePerformancesIntegrationMethods(exp1, 0.95, 10e-5, stat, rnd);
    System.out.println("=== UniformSampling ===");
    System.out.println("Number of runs needed: " + numberOfRunsNeededUniformSampling);
    System.out.println("Current estimator of G: " + stat.getAverage());

    stat.init();
  }
}
