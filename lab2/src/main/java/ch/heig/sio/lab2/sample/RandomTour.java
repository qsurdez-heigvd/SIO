package ch.heig.sio.lab2.sample;

import ch.heig.sio.lab2.display.ObservableTspConstructiveHeuristic;
import ch.heig.sio.lab2.display.TspHeuristicObserver;
import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;


/**
 * <p>Use an instance of {@link RandomTour} to build a tour starting at given city, and then iteratively connecting to a
 * randomly selected unvisited city.</p>
 *
 * <p>Instances of this class are immutable and thread-safe.</p>
 *
 * <p>Complexity (space and time): O(n)</p>
 */
public class RandomTour implements ObservableTspConstructiveHeuristic {

  private static final class Traversal implements Iterator<Edge> {

    /**
     * Number of cities in partial tour.
     */
    private final int n;

    /**
     * Start index (in partial tour) of next edge.
     */
    private int i = 0;

    /**
     * List of cities in current solution (partial tour).
     */
    private final int[] partialTour;

    Traversal(int[] partialTour, int n) {
      this.n = n;
      this.partialTour = partialTour;

      if (n > partialTour.length)
        throw new IllegalArgumentException("Number of edges to traverse ("+(n-1)+") should be smaller than tour length ("+partialTour.length+").");

      // Avoid self-loop if n = 1.
      if (n < 2)
        i = 1;
    }

    @Override
    public boolean hasNext() {
      // Traverse only the edges of actual path without closing it (i < n would close it).
      return i < (n - 1);
    }

    @Override
    public Edge next() {
      if (!hasNext())
        throw new NoSuchElementException();

      return new Edge(partialTour[i], partialTour[++i]);
    }
  }


  @Override
  public TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer) {

    // Number of cities in data set.
    int n = data.getNumberOfCities();

    // Array storing the (partial) tour.
    int[] tour = new int[n];

    // Create canonical tour (0,1,2,3,... n-1).
    for (int i = 0; i < n; ++i)
      tour[i] = i;

    // Move start city at first position.
    swap(tour, 0, startCityIndex);

    // Shuffle tour (but keep start city at position 0) using Fisher-Yates algorithm
    Random rnd = new Random();
    for (int i = n - 1; i > 2; --i) {
      int j = rnd.nextInt(i) + 1;
      swap(tour, i, j);
    }

    // Incrementally computed tour length.
    long length = 0;

    // Animation of cities insertion.
    for (int i = 1; i < n; i++) {
      // Update tour length.
      length += data.getDistance(tour[i - 1], tour[i]);

      // Update GUI.
      observer.update(new RandomTour.Traversal(tour, i + 1));
    }

    // Add the last distance (length of the final edge that closes the tour, back to start).
    length += data.getDistance(tour[n - 1], tour[0]);


/*
    // Checks - FOR DEBUGGING PURPOSE ONLY

    // Verify calculation of tour length.
    long distance_check = data.getDistance(tour[0], tour[n - 1]);
    for (int i = 1; i < n; ++i) distance_check += data.getDistance(tour[i - 1], tour[i]);
    if (length != distance_check)
      System.err.println("length != distance_check " + (length + " != " + distance_check));

    // Verify all cities are present in tour.
    int[] tour_check = java.util.Arrays.copyOf(tour, n);
    java.util.Arrays.sort(tour_check);
    for (int i = 0; i < n; ++i)
      if (tour_check[i] != i) {
        System.err.println("Error with Tour ! city index " + ((tour_check[i] > i) ? i + " not in tour." : tour_check[i]+ " found multiple times."));
        break;
      }
*/


    return new TspTour(data, tour, length);
  }


  /**
   * Swaps two elements in an array.
   *
   * @param array The array.
   * @param i1    The index of the first element.
   * @param i2    The index of the second element.
   */
  static void swap(int[] array, int i1, int i2) {
    int tmp = array[i1];
    array[i1] = array[i2];
    array[i2] = tmp;
  }

}

