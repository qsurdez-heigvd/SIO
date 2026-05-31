package ch.heig.sio.lab2.groupH;

import ch.heig.sio.lab2.display.ObservableTspConstructiveHeuristic;
import ch.heig.sio.lab2.display.TspHeuristicObserver;
import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.heig.sio.lab2.groupH.Utils.NearestCandidate;

import static ch.heig.sio.lab2.groupH.Utils.findNearestUnvisited;

/**
 * Nearest Neighbor (NN) heuristic for the Traveling Salesman Problem.
 *
 * <p>This greedy constructive heuristic builds a tour by starting at a specified city
 * and repeatedly moving to the nearest unvisited city until all cities have been visited.
 * The tour is then completed by returning to the starting city.</p>
 *
 * <p><strong>Algorithm Overview:</strong></p>
 * <ol>
 *   <li>Start at the specified city and mark it as visited</li>
 *   <li>While unvisited cities remain:
 *     <ul>
 *       <li>Find the nearest unvisited city to the current city</li>
 *       <li>Move to that city and add the edge to the tour</li>
 *       <li>Mark the city as visited</li>
 *     </ul>
 *   </li>
 *   <li>Close the tour by adding an edge from the last city back to the starting city</li>
 * </ol>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Time Complexity:</strong> O(n²) where n is the number of cities.
 *       Each of n cities requires finding the minimum among remaining unvisited cities.</li>
 *   <li><strong>Space Complexity:</strong> O(n) for the tour array and visited tracking</li>
 *   <li><strong>Tour Quality:</strong> Produces reasonable tours quickly, but quality
 *       is highly dependent on the starting city.</li>
 *   <li><strong>Deterministic:</strong> Given the same starting city, always produces
 *       the same tour</li>
 * </ul>
 *
 * <p>This implementation supports visualization through the {@link TspHeuristicObserver}
 * interface, allowing step-by-step animation of the tour construction process.</p>
 *
 * @author Quentin Surdez
 * @author Quentin Surdez
 *
 */
public class NearestNeighbor implements ObservableTspConstructiveHeuristic {

    /**
     * Iterator for animating the tour construction process.
     * Provides edges one at a time for visualization.
     *
     * <p>This iterator traverses the edges of a partial tour in sequence, providing
     * edges one at a time for visualization purposes. It does not include the closing
     * edge that would complete the tour cycle.</p>
     *
     * <p>The traversal operates on a partial tour, allowing visualization of the tour
     * as it grows during the construction process. The iterator can be configured to
     * traverse only the first n cities of a tour, enabling incremental display.</p>
     */
    private static final class Traversal implements Iterator<Edge> {

        /**
         * Number of cities in the partial tour to display.
         * This defines how many cities of the tour should be traversed for visualization.
         */
        private final int n;

        /**
         * Current position in the tour traversal (0-indexed).
         * Incremented with each call to {@link #next()} until all edges are returned.
         */
        private int i = 0;

        /**
         * The partial tour being constructed.
         * Contains city indices in the order they are visited.
         */
        private final int[] partialTour;

        /**
         * Creates a traversal iterator for animation of a partial tour.
         *
         * <p>The iterator will provide edges connecting the first n cities in the tour.
         * If the tour contains only one city, the iterator is initialized in a completed
         * state to avoid generating a self-loop edge.</p>
         *
         * @param partialTour The tour array containing city indices in visit order
         * @param n           Number of cities to traverse for incremental display
         *                    (must be ≥ 1 and ≤ partialTour.length)
         * @throws IllegalArgumentException if n is greater than the tour length
         */
        Traversal(int[] partialTour, int n) {
            this.n = n;
            this.partialTour = partialTour;

            if (n > partialTour.length) {
                throw new IllegalArgumentException(
                        "Number of edges to traverse (" + (n - 1) +
                                ") should be smaller than tour length (" + partialTour.length + ")."
                );
            }

            // Avoid self-loop if only one city
            if (n < 2) {
                i = 1;
            }
        }

        /**
         * Determines whether there are more edges to traverse.
         *
         * <p>Returns {@code true} if there are additional edges in the partial tour
         * that have not yet been returned. The traversal includes only the edges
         * connecting consecutive cities, not the closing edge back to the start.</p>
         *
         * @return {@code true} if more edges are available, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            // Traverse only the edges of the actual path without closing it
            return i < (n - 1);
        }

        /**
         * Returns the next edge in the traversal sequence.
         *
         * <p>Edges are returned in order as they appear in the tour construction.
         * Each edge connects the current city to the next city in the sequence.</p>
         *
         * @return The next edge connecting two consecutive cities in the tour
         * @throws NoSuchElementException if no more edges are available
         */
        @Override
        public Edge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return new Edge(partialTour[i], partialTour[++i]);
        }
    }

    /**
     * Computes a TSP tour using the nearest neighbor heuristic with step-by-step
     * observation support.
     *
     * <p>This method implements the core Nearest Neighbor algorithm, building a tour
     * by greedily selecting the nearest unvisited city at each step. The observer is
     * notified after each city addition, allowing for visualization or analysis of
     * the construction process.</p>
     *
     * @param data           The TSP instance containing city coordinates and distance matrix
     * @param startCityIndex The index of the starting city (must be valid: 0 ≤ index < n)
     * @param observer       Observer to receive updates during tour construction;
     *                       called once per added city with the current partial tour state
     * @return A complete TSP tour containing all cities with the computed total length,
     *         including the closing edge from the last city back to the starting city
     *
     * @see Utils#findNearestUnvisited(TspData, int, boolean[])
     */
    @Override
    public TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer) {

        // Number of cities in the dataset
        final int n = data.getNumberOfCities();

        // Array to store the tour as it's built
        int[] tour = new int[n];

        // Track which cities have been visited
        boolean[] visited = new boolean[n];

        // Start with the specified city
        tour[0] = startCityIndex;
        visited[startCityIndex] = true;

        // Track the cumulative tour length
        long length = 0;

        // Build the tour by selecting nearest neighbor at each step
        for (int step = 1; step < n; step++) {
            int currentCity = tour[step - 1];

            NearestCandidate nearestToHead = findNearestUnvisited(data, currentCity, visited);

            // Add the nearest city to the tour
            tour[step] = nearestToHead.city();
            visited[nearestToHead.city()] = true;
            length += nearestToHead.distance();

            // Notify observer for animation (GUI update)
            observer.update(new Traversal(tour, step + 1));
        }

        // Close the tour: add distance from last city back to start
        length += data.getDistance(tour[n - 1], tour[0]);

        return new TspTour(data, tour, length);
    }

    /**
     * Computes a TSP tour using the nearest neighbor heuristic.
     *
     * <p>This is a convenience method that delegates to
     * {@link #computeTour(TspData, int, TspHeuristicObserver)} with a no-op observer,
     * allowing tour computation without visualization.</p>
     *
     * <p>This method is typically used when only the final tour result is needed,
     * without step-by-step visualization or intermediate state tracking.</p>
     *
     * @param data           The TSP instance containing cities and distances
     * @param startCityIndex The index of the city to begin tour construction (0-indexed)
     * @return A complete TSP tour with computed total length
     */
    @Override
    public TspTour computeTour(TspData data, int startCityIndex) {
        return computeTour(data, startCityIndex, traversal -> {
        });
    }

}