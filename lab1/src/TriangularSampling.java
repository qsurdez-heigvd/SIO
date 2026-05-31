import montecarlo.Experiment;

import java.util.Random;

public class TriangularSampling implements Experiment {
  private final double a;
  private final double b;
  private final double c;
  private final double height;

  TriangularSampling(double a, double b, double c, double height) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.height = height;
  }

  @Override
  public double execute(Random rnd) {
    double u = rnd.nextDouble();

    return 0;
  }
}
