package ch.heig.sio.lab2.groupH;

import ch.heig.sio.lab2.display.HeuristicComboItem;
import ch.heig.sio.lab2.display.TspSolverGui;
import ch.heig.sio.lab2.sample.CanonicalTour;
import ch.heig.sio.lab2.sample.RandomTour;
import com.formdev.flatlaf.FlatLightLaf;

public final class Gui {
  public static void main(String[] args) {
    HeuristicComboItem[] heuristics = {
        new HeuristicComboItem("Canonical tour", new CanonicalTour()),
        new HeuristicComboItem("Random tour", new RandomTour()),

        new HeuristicComboItem("Nearest Neighbor", new NearestNeighbor()),
        new HeuristicComboItem("DENN", new DoubleEndedNearestNeighbour()),
    };

    // May not work on all platforms, comment out if necessary
    System.setProperty("sun.java2d.opengl", "true");

    FlatLightLaf.setup();
    new TspSolverGui(1400, 800, "TSP solver", heuristics);
  }
}
