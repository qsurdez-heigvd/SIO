package ch.heig.sio.lab2.tsp;

import java.util.Arrays;

/**
 * Record storing a solution for an instance of the TSP.
 * @param data Reference to problem instance
 * @param tour Mutable array storing the permutation of city indices in the tour
 * @param length Length of the tour
 */
public record TspTour(TspData data, int[] tour, long length) {
	/**
	 * @return String representation of the current tour
	 */
	public String toString() {
		return "Length: " + length + ", "
				+ "Tour: " + Arrays.toString(tour);
	}
}