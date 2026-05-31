import montecarlo.Experiment;

import java.util.Random;

public class AcceptanceRejection implements Experiment {
    private final double a;
    private final double b;
    private final double M;


    AcceptanceRejection(double a, double b, double M) {
        this.a = a;
        this.b = b;
        this.M = M;
    }

    @Override
    public double execute(Random rnd) {
        double x = a + rnd.nextDouble() * (b-a);    // translating to the right space [a,b]
        double y = rnd.nextDouble() * M;            // translating to the right space [0,M]

        if (y <= IntegralFunction.g(x)) {
            return M * (b-a);
        } else {
            return 0;
        }
    }
}