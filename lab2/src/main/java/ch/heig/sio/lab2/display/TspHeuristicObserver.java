package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.Edge;

import java.util.Iterator;

/**
 * <p>An observer for a TSP heuristic.</p>
 *
 * <p>An update provides all the edges to display as an {@link Iterator}. This is for convenience of the display,
 * <b>this does not imply that the heuristic is required to store all the edges of the tour.</b></p>
 */
public interface TspHeuristicObserver {
  /**
   * <p>Update the observer with the current edges.</p>
   *
   * @param edges Current edges.
   */
  void update(Iterator<Edge> edges);
}
