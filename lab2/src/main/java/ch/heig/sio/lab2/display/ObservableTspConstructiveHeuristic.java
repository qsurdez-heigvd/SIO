package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.TspConstructiveHeuristic;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

/**
 * <p>Observable version of {@link TspConstructiveHeuristic}, for use in {@link TspSolverGui}.</p>
 */
public interface ObservableTspConstructiveHeuristic extends TspConstructiveHeuristic {
  @Override
  default TspTour computeTour(TspData data, int startCity) {
    return computeTour(data, startCity, v -> {});
  }

  /**
   * <p>Extension of {@link TspConstructiveHeuristic#computeTour} with a {@link TspHeuristicObserver}.</p>
   *
   * @param data           Data of problem instance
   * @param startCityIndex Starting city
   * @param observer       Observer to notify of the progress
   * @return Solution found by the heuristic
   *
   * @throws NullPointerException      if {@code data} or {@code observer} is null
   * @throws IndexOutOfBoundsException if {@code startCityIndex} is negative or greater than the number of cities
   * @throws IllegalArgumentException  if {@code startCityIndex} is invalid regarding implementation's criteria
   */
  TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer);
}
