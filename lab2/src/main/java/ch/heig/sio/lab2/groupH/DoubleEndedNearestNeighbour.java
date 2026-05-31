package ch.heig.sio.lab2.groupH;

import ch.heig.sio.lab2.display.ObservableTspConstructiveHeuristic;
import ch.heig.sio.lab2.display.TspHeuristicObserver;
import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static ch.heig.sio.lab2.groupH.Utils.findNearestUnvisited;

import ch.heig.sio.lab2.groupH.Utils.NearestCandidate;

/**
 * Double-Ended Nearest Neighbor (DENN) heuristic for the Traveling Salesman Problem.
 *
 * <p>This constructive heuristic builds a tour by growing it from both ends simultaneously.
 * Starting from an initial city, the algorithm maintains a partial tour with a head and tail.
 * At each iteration, it identifies the nearest unvisited city to both the head and tail,
 * then adds the closer of these two candidates to the appropriate end of the tour.</p>
 *
 * <p><strong>Algorithm Overview:</strong></p>
 * <ol>
 *   <li>Initialize the tour with a single starting city</li>
 *   <li>While unvisited cities remain:
 *     <ul>
 *       <li>Find the nearest unvisited city to the head</li>
 *       <li>Find the nearest unvisited city to the tail</li>
 *       <li>Add the closer candidate to the corresponding end (head or tail)</li>
 *       <li>Mark the added city as visited</li>
 *     </ul>
 *   </li>
 *   <li>Close the tour by connecting the final head and tail cities</li>
 * </ol>
 *
 * <p><strong>Performance Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Time Complexity:</strong> O(n²) where n is the number of cities</li>
 *   <li><strong>Space Complexity:</strong> O(n) for the tour array and visited tracking</li>
 *   <li><strong>Tour Quality:</strong> Generally produces better tours than single-ended
 *       nearest neighbor by considering growth from both directions</li>
 * </ul>
 *
 * <p><strong>Implementation Details:</strong></p>
 * <p>The tour is built using an array of size 2n with the initial city placed at the center.
 * This design allows for efficient growth in both directions without array shifting.
 * Head and tail indices track the current boundaries of the partial tour, moving inward
 * (head decreasing) or outward (tail increasing) as cities are added.</p>
 *
 * <p>This implementation supports visualization through the {@link TspHeuristicObserver}
 * interface, allowing step-by-step animation of the tour construction process.</p>
 *
 * @author Quentin Surdez
 * @author Quentin Surdez
 *
 */
public class DoubleEndedNearestNeighbour implements ObservableTspConstructiveHeuristic {

    /**
     * Iterator for animating the tour construction process.
     * Handles the visualization of a tour that grows from both ends.
     *
     * <p>This iterator traverses the edges of a partial tour in sequence from head to tail,
     * providing edges one at a time for visualization purposes. It does not include the
     * closing edge that would complete the tour cycle.</p>
     *
     * <p>The traversal operates on a window within the full tour array, defined by the
     * head index and tour size, allowing visualization of the tour as it grows during
     * the construction process.</p>
     */
    private static final class Traversal implements Iterator<Edge> {

        /**
         * Number of cities currently in the partial tour.
         * This defines the length of the tour segment being traversed.
         */
        private final int tourSize;

        /**
         * Current position in traversal relative to the start of the partial tour.
         * Incremented with each call to {@link #next()} until all edges are returned.
         */
        private int currentIndex = 0;

        /**
         * Starting index in the full tour array (head position).
         * This marks the beginning of the partial tour segment within the 2n-sized array.
         */
        private final int headIndex;

        /**
         * The complete tour array containing all city indices.
         * The partial tour occupies positions from headIndex to (headIndex + tourSize - 1).
         */
        private final int[] tour;

        /**
         * Creates a traversal iterator for animation of a partial tour.
         *
         * <p>If the tour contains only one city, the iterator is initialized in a
         * completed state to avoid generating a self-loop edge.</p>
         *
         * @param tour      The full tour array of size 2n containing city indices
         * @param headIndex The starting position of the partial tour within the array
         * @param tourSize  Number of cities currently in the partial tour (must be ≥ 1)
         */
        Traversal(int[] tour, int headIndex, int tourSize) {
            this.tour = tour;
            this.headIndex = headIndex;
            this.tourSize = tourSize;

            // Avoid self-loop if only one city
            if (tourSize < 2) {
                currentIndex = tourSize;
            }
        }

        /**
         * Determines whether there are more edges to traverse.
         *
         * <p>Returns {@code true} if there are additional edges in the partial tour
         * that have not yet been returned. The traversal includes all edges connecting
         * consecutive cities but does not include the closing edge back to the start.</p>
         *
         * @return {@code true} if more edges are available, {@code false} otherwise
         */
        @Override
        public boolean hasNext() {
            // Traverse edges without closing the tour
            return currentIndex < (tourSize - 1);
        }

        /**
         * Returns the next edge in the traversal sequence.
         *
         * <p>Edges are returned in order from the head to the tail of the partial tour.
         * Each edge connects two consecutive cities in the current tour segment.</p>
         *
         * @return The next edge connecting two consecutive cities in the tour
         * @throws NoSuchElementException if no more edges are available
         */
        @Override
        public Edge next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            int cityA = tour[headIndex + currentIndex];
            int cityB = tour[headIndex + currentIndex + 1];
            currentIndex++;

            return new Edge(cityA, cityB);
        }
    }

    /**
     * Computes a TSP tour using the double-ended nearest neighbor heuristic.
     *
     * <p>This is a convenience method that delegates to
     * {@link #computeTour(TspData, int, TspHeuristicObserver)} with a no-op observer,
     * allowing tour computation without visualization.</p>
     *
     * @param data      The TSP instance containing cities and distances
     * @param startCity The index of the city to begin tour construction (0-indexed)
     * @return A complete TSP tour with computed total length
     */
    @Override
    public TspTour computeTour(TspData data, int startCity) {
        return computeTour(data, startCity, traversal -> {
        });
    }

    /**
     * Computes a TSP tour using the double-ended nearest neighbor heuristic with
     * step-by-step observation support.
     *
     * <p>This method implements the core DENN algorithm, building a tour by alternately
     * adding cities to the head or tail based on which end has the nearest unvisited
     * neighbor. The observer is notified after each city addition, allowing for
     * visualization or analysis of the construction process.</p>
     *
     * <p><strong>Array Strategy:</strong> The algorithm uses a 2n-sized array to avoid
     * costly array operations. The starting city is placed at index n-1, providing n
     * positions on each side for growth. This eliminates the need for array shifting
     * or resizing during construction.</p>
     *
     * @param data           The TSP instance containing city coordinates and distance matrix
     * @param startCityIndex The index of the starting city (must be valid: 0 ≤ index < n)
     * @param observer       Observer to receive updates during tour construction;
     *                       called once per added city with the current partial tour state
     * @return A complete TSP tour containing all cities with the computed total length,
     *         including the closing edge from the last city back to the first
     *
     */
    @Override
    public TspTour computeTour(TspData data, int startCityIndex, TspHeuristicObserver observer) {
        final int n = data.getNumberOfCities();

        // To handle worst case scenario where only adding on one side
        int[] tour = new int[n * 2];
        boolean[] visited = new boolean[n];

        // Set the headIndex and tailIndex to the middle of the array created so that
        // there is n index on one side and on the other
        int headIndex = n - 1;
        int tailIndex = headIndex;
        tour[headIndex] = startCityIndex;
        visited[startCityIndex] = true;

        long length = 0;

        for (int step = 1; step < n; step++) {
            int headCity = tour[headIndex];
            int tailCity = tour[tailIndex];

            NearestCandidate nearestToHead = findNearestUnvisited(data, headCity, visited);
            NearestCandidate nearestToTail = findNearestUnvisited(data, tailCity, visited);

            NearestCandidate chosen;
            if (nearestToHead.distance() <= nearestToTail.distance()) {
                headIndex--;
                tour[headIndex] = nearestToHead.city();
                chosen = nearestToHead;
            } else {
                tailIndex++;
                tour[tailIndex] = nearestToTail.city();
                chosen = nearestToTail;
            }

            visited[chosen.city()] = true;
            length += chosen.distance();
            observer.update(new Traversal(tour, headIndex, step + 1));
        }

        length += data.getDistance(tour[tailIndex], tour[headIndex]);
        // We send the final slice of interest from the array
        // all the unused space is removed
        int[] finalTour = new int[n];
        System.arraycopy(tour, headIndex, finalTour, 0, n);
        return new TspTour(data, finalTour, length);
    }
}