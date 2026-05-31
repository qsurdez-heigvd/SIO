import montecarlo.Experiment;

import java.util.Random;

public class UniformSamplingMethod implements Experiment {

    private final double a;
    private final double b;


    UniformSamplingMethod(double a, double b) {
        this.a = a;
        this.b = b;
    }


    @Override
    public double execute(Random rnd) {
        double x = a + rnd.nextDouble() * (b-a);

        double fx = 1.0 / (b-a);

        return IntegralFunction.g(x) / fx;
    }
}
