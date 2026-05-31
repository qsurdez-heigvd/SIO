package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

/**
 * Thin wrapper around {@link ObservableTspConstructiveHeuristic} to display heuristic name in combo box.
 */
public final class HeuristicComboItem implements ObservableTspConstructiveHeuristic {
  private final String name;
  private final ObservableTspConstructiveHeuristic heuristic;

  public HeuristicComboItem(String name, ObservableTspConstructiveHeuristic heuristic) {
    this.name = name;
    this.heuristic = heuristic;
  }

  @Override
  public TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer) {
    return heuristic.computeTour(data, startCityIndex, observer);
  }

  @Override
  public String toString() {
    return name;
  }
}
