package ch.heig.sio.lab2.groupH;

import ch.heig.sio.lab2.tsp.TspData;

/**
 * Utility methods for TSP heuristic algorithms.
 *
 * <p>This class provides common helper functions used across different TSP heuristic
 * implementations, including methods for finding nearest unvisited cities and managing
 * tour construction operations.</p>
 *
 * <p>All methods in this class are static utilities and the class is not designed to
 * be instantiated.</p>
 *
 * @author Quentin Surdez
 * @author Quentin Surdez
 */
public class Utils {

    /**
     * Represents a candidate city with its distance from a reference point.
     *
     * <p>This record encapsulates the result of a nearest neighbor search, storing
     * both the city index and the distance to that city. It is used primarily as a
     * return type for nearest city queries in TSP heuristics.</p>
     *
     * @param city     The index of the candidate city (0-indexed)
     * @param distance The distance from the reference city to this candidate
     */
    public record NearestCandidate(int city, long distance) {
    }

    /**
     * Finds the nearest unvisited city to a given city.
     *
     * <p>This method performs a linear search through all cities to identify the
     * unvisited city with the minimum distance from the specified starting city.
     * The search only considers cities marked as unvisited in the provided boolean array.</p>
     *
     * <p><strong>Performance Characteristics:</strong></p>
     * <ul>
     *   <li><strong>Time Complexity:</strong> O(n) where n is the number of cities</li>
     *   <li><strong>Space Complexity:</strong> O(1) - uses only constant extra space</li>
     * </ul>
     *
     * @param data     The TSP instance containing the distance matrix between cities
     * @param fromCity The index of the reference city from which to measure distances
     * @param visited  Boolean array tracking visited cities; {@code true} indicates
     *                 a city has been visited and should be excluded from consideration
     * @return A {@link NearestCandidate} containing the index and distance of the
     *         nearest unvisited city
     * @throws IllegalStateException if all cities have been visited (no unvisited city found)
     */
    public static NearestCandidate findNearestUnvisited(TspData data, int fromCity, boolean[] visited) {
        int nearestCity = -1;
        long shortestDistance = Long.MAX_VALUE;

        int n = data.getNumberOfCities();

        // Find the nearest unvisited city
        for (int city = 0; city < n; city++) {
            if (!visited[city]) {
                long distance = data.getDistance(fromCity, city);
                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestCity = city;
                }
            }
        }

        // Should never happen but error handling is important
        if (nearestCity == -1) {
            throw new IllegalStateException("No unvisited city found");
        }

        return new NearestCandidate(nearestCity, shortestDistance);
    }
}