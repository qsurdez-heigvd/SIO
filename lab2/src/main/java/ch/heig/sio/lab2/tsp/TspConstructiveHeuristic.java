package ch.heig.sio.lab2.tsp;

/**
 * A constructive heuristic for the TSP, that will compute a tour for a given instance by adding cities progressively.
 */
@FunctionalInterface
public interface TspConstructiveHeuristic {
  /**
   * <p>Computes a tour for the symmetric travelling salesman problem on the given instance.</p>
   *
   * <p>The {@code startCity} parameter can be ignored when not needed by the implementation.</p>
   *
   * @param data      Data of problem instance
   * @param startCity Starting city, if needed by the implementation
   * @return Solution found by the heuristic
   *
   * @throws NullPointerException      if {@code data} is null
   * @throws IndexOutOfBoundsException if {@code startCity} is negative or greater than the number of cities, may not be
   *                                   thrown if {@code startCity} is ignored
   * @throws IllegalArgumentException  if {@code startCity} is invalid regarding implementation's criteria
   */
  TspTour computeTour(TspData data, int startCity);
}
