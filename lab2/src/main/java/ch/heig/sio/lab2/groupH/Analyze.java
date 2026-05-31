package ch.heig.sio.lab2.groupH;

import ch.heig.sio.lab2.tsp.TspConstructiveHeuristic;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspTour;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive analysis tool for TSP heuristics.
 * Evaluates algorithms across multiple instances and starting cities,
 * computing performance statistics and execution times.
 *
 * @author Quentin Surdez
 * @author Quentin Surdez
 */
public final class Analyze {

    /**
     * Instance configuration with optimal tour length.
     */
    private record Instance(String filename, long optimalLength) {
    }

    /**
     * Statistics for a single heuristic on a single instance.
     */
    private static class HeuristicResults {
        private final String heuristicName;
        private final String instanceName;
        private final long optimalLength;
        private final List<Long> tourLengths;
        private long executionTimeMs;

        // Computed incrementally as results are added
        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;
        private long sum = 0;
        private int count = 0;

        public HeuristicResults(String heuristicName, String instanceName, long optimalLength) {
            this.heuristicName = heuristicName;
            this.instanceName = instanceName;
            this.optimalLength = optimalLength;
            this.tourLengths = new ArrayList<>();
        }

        public void addResult(long tourLength) {
            tourLengths.add(tourLength);

            // Update statistics incrementally
            if (tourLength < min) {
                min = tourLength;
            }
            if (tourLength > max) {
                max = tourLength;
            }
            sum += tourLength;
            count++;
        }

        public void setExecutionTime(long timeMs) {
            this.executionTimeMs = timeMs;
        }

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }

        public double getMean() {
            return count == 0 ? 0.0 : (double) sum / count;
        }

        public double getRelativeMin() {
            return (double) min / optimalLength;
        }

        public double getRelativeMax() {
            return (double) max / optimalLength;
        }

        public double getRelativeMean() {
            return getMean() / optimalLength;
        }

        public void printResults() {
            System.out.println("┌─────────────────────────────────────────────────────────────────────┐");
            System.out.printf("│ %-67s │%n", heuristicName + " - " + instanceName);
            System.out.println("├─────────────────────────────────────────────────────────────────────┤");
            System.out.printf("│ Optimal length:        %10d                                │%n", optimalLength);
            System.out.printf("│ Executions:            %10d (one per starting city)          │%n", tourLengths.size());
            System.out.printf("│ Total execution time:  %10d ms                              │%n", executionTimeMs);
            System.out.println("├─────────────────────────────────────────────────────────────────────┤");
            System.out.println("│ ABSOLUTE PERFORMANCE                                                │");
            System.out.printf("│   Minimum:             %10d                                │%n", getMin());
            System.out.printf("│   Mean:                %10.2f                                │%n", getMean());
            System.out.printf("│   Maximum:             %10d                                │%n", getMax());
            System.out.println("├─────────────────────────────────────────────────────────────────────┤");
            System.out.println("│ RELATIVE PERFORMANCE (vs optimal)                                   │");
            System.out.printf("│   Minimum:             %10.4f  (%+.2f%%)                      │%n",
                    getRelativeMin(), (getRelativeMin() - 1.0) * 100);
            System.out.printf("│   Mean:                %10.4f  (%+.2f%%)                      │%n",
                    getRelativeMean(), (getRelativeMean() - 1.0) * 100);
            System.out.printf("│   Maximum:             %10.4f  (%+.2f%%)                      │%n",
                    getRelativeMax(), (getRelativeMax() - 1.0) * 100);
            System.out.println("└─────────────────────────────────────────────────────────────────────┘");
            System.out.println();
        }
    }

    /**
     * Test configuration pairing a heuristic with its name.
     */
    private record HeuristicConfig(String name, TspConstructiveHeuristic heuristic) {
    }

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("           TSP HEURISTICS COMPREHENSIVE ANALYSIS");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println();

        // Define test instances with optimal lengths
        Instance[] instances = {
                new Instance("pcb442.dat", 50_778),
                new Instance("att532.dat", 86_729),
                new Instance("u574.dat", 36_923),
                new Instance("pcb1173.dat", 56_892),
                new Instance("nrw1379.dat", 56_638),
                new Instance("u1817.dat", 57_201)
        };

        // Define heuristics to test
        HeuristicConfig[] heuristics = {
                new HeuristicConfig("Nearest Neighbor", new NearestNeighbor()),
                new HeuristicConfig("Double-Ended Nearest Neighbor", new DoubleEndedNearestNeighbour())
        };

        // Run analysis for each combination
        for (Instance instance : instances) {
            for (HeuristicConfig heuristic : heuristics) {
                analyzeHeuristic(instance, heuristic);
            }
        }

        System.out.println("═══════════════════════════════════════════════════════════════════════");
        System.out.println("                        ANALYSIS COMPLETE");
        System.out.println("═══════════════════════════════════════════════════════════════════════");
    }

    /**
     * Analyzes a single heuristic on a single instance across all starting cities.
     *
     * @param instance  The TSP instance to solve
     * @param heuristic The heuristic to evaluate
     */
    private static void analyzeHeuristic(Instance instance, HeuristicConfig heuristic) {
        try {
            // Load data
            TspData data = TspData.fromFile("data/" + instance.filename);
            int n = data.getNumberOfCities();

            HeuristicResults results = new HeuristicResults(
                    heuristic.name,
                    instance.filename,
                    instance.optimalLength
            );

            // Measure execution time for all starting cities
            long startTime = System.currentTimeMillis();

            // Test each city as starting point
            for (int startCity = 0; startCity < n; startCity++) {
                TspTour tour = heuristic.heuristic.computeTour(data, startCity);
                results.addResult(tour.length());
            }

            long endTime = System.currentTimeMillis();
            results.setExecutionTime(endTime - startTime);

            // Display results
            results.printResults();

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Could not find instance file: " + instance.filename);
            System.err.println("       Make sure the data directory is in the correct location.");
            System.err.println();
        }
    }
}