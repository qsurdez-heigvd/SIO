package ch.heig.sio.lab2.tsp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * <p>Class storing immutable data for an instance of the TSP.</p>
 *
 * <p>Access to distance between cities is provided in constant time to the number of cities, result may or may not
 * be precomputed depending on the amount of available memory.</p>
 *
 * <p>Each instance contains at least one city.</p>
 */
public final class TspData {
  private static final int MIN_CITIES = 1;

  /** Array of cities in the TSP instance. */
  private final City[] cities;
  /** Symmetric matrix of distances between cities. Null if not enough memory is available to store it. */
  private final int[][] distanceMatrix;

  /**
   * Creates a new TspData.
   *
   * @param cities         Array of cities.
   * @param distanceMatrix Matrix of distances between cities.
   */
  private TspData(final City[] cities, final int[][] distanceMatrix) {
    this.cities = cities;
    this.distanceMatrix = distanceMatrix;
  }

  /**
   * Creates a new TspData instance from a text file containing cities' data.
   *
   * @param filename name of the file to read from.
   * @throws FileNotFoundException If file can't be found.
   * @throws TspParsingException   If file content does not conform to expected format.
   * @throws OutOfMemoryError      If the number of cities is too large.
   */
  public static TspData fromFile(final String filename) throws TspParsingException, FileNotFoundException {
    try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(filename)))) {
      // Check that inputStream is open and not empty
      try {
        if (!scanner.hasNext()) {
          throw new TspParsingException("Invalid data. Empty data.");
        }
      } catch (IllegalStateException e) {
        throw new TspParsingException("Invalid data. Unable to read data.");
      }

      // Read the number of cities
      int numberOfCities;
      try {
        numberOfCities = scanner.nextInt();
      } catch (InputMismatchException e) {
        throw new TspParsingException("Invalid data value. Invalid number of cities in first line of data file.");
      } catch (NoSuchElementException e) {
        throw new TspParsingException("Invalid data format. Empty data file.");
      }
      if (numberOfCities < MIN_CITIES) {
        throw new TspParsingException("Invalid data value. Number of cities should be at least " + MIN_CITIES + ".");
      }


      // Allocate the array storing the XY coordinates of the cities
      City[] cities;
      try {
        cities = new City[numberOfCities];
      } catch (OutOfMemoryError e) {
        throw new OutOfMemoryError("Out of memory error. Number of cities is too large.");
      }

      // Read the coordinates of each city
      for (int cityReadCount = 0; cityReadCount < numberOfCities; cityReadCount++) {
        try {
          int cityNumber = scanner.nextInt();
          if (cityNumber != cityReadCount) {
            throw new TspParsingException(
                String.format("Invalid city number: %s expected, %s read.", cityNumber, cityReadCount));
          }
          int x = scanner.nextInt();
          int y = scanner.nextInt();
          cities[cityNumber] = new City(x, y);
        } catch (InputMismatchException e) {
          throw new TspParsingException("Invalid data value. City numbers and coordinates should be non negative integers.");
        } catch (NoSuchElementException e) {
          throw new TspParsingException(
              "Incomplete line : should follow format \"<city number> <u> <v>\""
          );
        } catch (IllegalStateException e) {
          throw new TspParsingException("Invalid data. Unable to read data.");
        }
      }

      return new TspData(cities, computeDistanceMatrix(cities));
    }
  }

  /**
   * <p>Creates a new TspData instance from an array of {@link City}.</p>
   *
   * <p>Cities indexes are used as city numbers.</p>
   *
   * @param cities cities.
   * @throws IllegalArgumentException if {@code cities} is empty
   * @throws NullPointerException     if {@code cities} or one of its elements is null
   */
  public static TspData fromArray(City[] cities) {
    if (cities.length == 0) {
      throw new IllegalArgumentException("cities array should not be empty.");
    }
    return new TspData(Arrays.copyOf(cities, cities.length), computeDistanceMatrix(cities));
  }

  private static int[][] computeDistanceMatrix(City[] cities) {
    // Try to allocate the distance matrix between cities.
    // If not enough space is available, set distanceMatrix to null (distances will have to be recomputed
    // each time in getDistance(i,j)).
    int[][] distanceMatrix;
    try {
      distanceMatrix = new int[cities.length][cities.length];
    } catch (OutOfMemoryError e) {
      return null;
    }

    for (int i = 0; i < cities.length; i++) {
      distanceMatrix[i][i] = 0;
      for (int j = 0; j < i; j++) {
        distanceMatrix[i][j] = distanceMatrix[j][i] =
            (int) Math.round(Math.hypot(cities[i].x - cities[j].x, cities[i].y - cities[j].y));
      }
    }

    return distanceMatrix;
  }

  /**
   * Returns the distance between two cities.
   *
   * @param i First city index.
   * @param j Second city index.
   * @return Distance between the two cities.
   *
   * @throws IndexOutOfBoundsException If i or j are out of bounds.
   */
  public int getDistance(int i, int j) {
    assertInBounds(i);
    assertInBounds(j);

    // If distanceMatrix was not allocated, compute distance from i to j.
    if (distanceMatrix == null) {
      return (int) Math.round /*Math.rint*/  (Math.hypot(cities[i].x - cities[j].x, cities[i].y - cities[j].y));
    } else {
      return distanceMatrix[i][j];
    }
  }

  /**
   * Returns the number of cities of this problem instance.
   *
   * @return Number of cities.
   */
  public int getNumberOfCities() {
    return cities.length;
  }

  /**
   * Returns cartesian coordinates of the city.
   *
   * @param city City index
   * @return City coordinates
   *
   * @throws IndexOutOfBoundsException If city is out of bounds.
   */
  public City getCityCoord(int city) {
    assertInBounds(city);
    return cities[city];
  }

  /**
   * Asserts that the index is in bounds.
   *
   * @param i Index to check.
   * @throws IndexOutOfBoundsException If i is out of bounds.
   */
  private void assertInBounds(int i) {
    if (i < 0 || i >= cities.length) {
      throw new IndexOutOfBoundsException("City index {" + i + "} out of bounds. Domain: [0, " + cities.length + "[");
    }
  }

  /**
   * Static nested class storing cities' XY coordinates
   */
  public record City(int x, int y) {
  }
}
