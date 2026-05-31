public class IntegralFunction {
    public static double g(double x) {
        return Math.exp(-x/8.0) * Math.abs(Math.sin((Math.PI * x) / 2.0)) * Math.sqrt(6-x);
    }
}
