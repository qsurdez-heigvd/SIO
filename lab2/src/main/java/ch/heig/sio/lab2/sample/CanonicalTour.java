package ch.heig.sio.lab2.sample;

import ch.heig.sio.lab2.display.ObservableTspConstructiveHeuristic;
import ch.heig.sio.lab2.display.TspHeuristicObserver;
import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>Use an instance of {@link CanonicalTour} to generate a canonical tour (0, 1, ..., n-1, 0)
 * for a TSP instance.</p>
 *
 * <p>Instances of this class are immutable and thread-safe.</p>
 *
 * <p>Complexity (space and time): O(n)</p>
 */
public final class CanonicalTour implements ObservableTspConstructiveHeuristic {
  /**
   * A traversal of the edges of the canonical tour.
   */
  private static final class Traversal implements Iterator<Edge> {
    /** Number of cities. */
    private final int n;

    /** Start index of next edge. */
    private int i = 0;

    Traversal(int n) {
      this.n = n;

      // Avoid self-loop if n = 1.
      if (n < 2)
        i = 1;
    }

    @Override
    public boolean hasNext() {
      return i < (n - 1);
    }

    @Override
    public Edge next() {
      if (!hasNext())
        throw new NoSuchElementException();
      return new Edge(i, ++i % n);
    }
  }

  @Override
  public TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer) {
    int n = data.getNumberOfCities();
    int[] tour = new int[n];
    // Initialize to the distance from the last city to the first city (will close the tour).
    long length = data.getDistance(n - 1, 0);

    // Insert cities 1 to n-1 in order (0 is already in the tour).
    for (int i = 1; i < n; i++) {
      tour[i] = i;
      length += data.getDistance(i - 1, i);

      observer.update(new Traversal(i + 1));
    }

      return new TspTour(data, tour, length);
  }



}
