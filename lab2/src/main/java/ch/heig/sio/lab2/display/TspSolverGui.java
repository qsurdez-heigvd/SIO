package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;
import ch.heig.sio.lab2.tsp.TspParsingException;
import ch.heig.sio.lab2.tsp.TspTour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * GUI class for the TSPSolver.
 *
 * @author Noah Boegli
 */
public final class TspSolverGui {
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newVirtualThreadPerTaskExecutor();
  private static final Random RND = new Random();

  private Animation animation;

  private TspData currentData;
  private int delay;
  private String filePath;

  // Swing components
  private final JFrame window;
  private final JPanel optionsPanel;
  private final JPanel topOptionsPanel;
  private final JPanel bottomOptionsPanel;
  private final JLabel currentFileLabel;
  private final JLabel resultsLabel;
  private final FileDialog dataFileDialog;
  private final JLabel speedChoiceLabel;
  private final JButton runContinuouslyButton;
  private final JButton stopRunButton;
  private final JButton runOneStepButton;
  private final JButton resetButton;
  private final JButton showFilePickerButton;
  private final JLabel heuristicChoiceLabel;
  private final JComboBox<ObservableTspConstructiveHeuristic> heuristicChoice;
  private final JLabel startCityIndexChoiceLabel;
  private final JComboBox<Integer> startCityIndexChoice;
  private final JComboBox<String> speedChoice;
  private final JCheckBox showCityLabels;
  private final TspDisplayArea displayArea;

  // Configuration for the topbar height
  private static final int TOPBAR_HEIGHT = 40;

  /**
   * Private constructor, used by `getInstance()`.
   *
   * @param width  The window width.
   * @param height The window height.
   * @param title  The window title.
   */
  public TspSolverGui(int width, int height, String title, HeuristicComboItem[] heuristics) {
    ////////////////////////////////////////////////////////////
    // Class setup
    ////////////////////////////////////////////////////////////
    this.delay = 100;

    ////////////////////////////////////////////////////////////
    // Base window
    ////////////////////////////////////////////////////////////
    this.window = new JFrame();
    this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.window.setSize(width, height);
    this.window.setTitle(title);
    window.setLayout(new BorderLayout());
    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent windowEvent) {
        if (animation != null)
          animation.cancel();
      }
    });

    ////////////////////////////////////////////////////////////
    // Main canvas
    ////////////////////////////////////////////////////////////
    this.displayArea = new TspDisplayArea();
    this.displayArea.setSize(width, height - 2 * TspSolverGui.TOPBAR_HEIGHT);


    ////////////////////////////////////////////////////////////
    // Options bar (topbar) component
    ////////////////////////////////////////////////////////////
    this.optionsPanel = new JPanel();
    this.optionsPanel.setPreferredSize(new Dimension(width, 2 * TspSolverGui.TOPBAR_HEIGHT));
    this.optionsPanel.setBounds(0, 0, width, 2 * TspSolverGui.TOPBAR_HEIGHT);
    this.topOptionsPanel = new JPanel();
    this.topOptionsPanel.setPreferredSize(new Dimension(width, TspSolverGui.TOPBAR_HEIGHT));
    this.topOptionsPanel.setBounds(0, 0, width, TspSolverGui.TOPBAR_HEIGHT);
    this.bottomOptionsPanel = new JPanel();
    this.bottomOptionsPanel.setPreferredSize(new Dimension(width, TspSolverGui.TOPBAR_HEIGHT));
    this.bottomOptionsPanel.setBounds(0, 0, width, TspSolverGui.TOPBAR_HEIGHT);


    ////////////////////////////////////////////////////////////
    // Configuration components
    ////////////////////////////////////////////////////////////

    // File picker & associated button (with label)
    this.showFilePickerButton = new JButton("Select data set");
    this.currentFileLabel = new JLabel("Current file: None");
    this.dataFileDialog = new FileDialog(this.window);
    dataFileDialog.setFilenameFilter((dir, name) -> name.endsWith(".dat"));
    showFilePickerButton.addActionListener(e -> {
      dataFileDialog.setDirectory(Path.of("").toAbsolutePath().toString());
      dataFileDialog.setVisible(true);

      var dir = dataFileDialog.getDirectory();
      var file = dataFileDialog.getFile();
      if (dir == null || file == null) {
        return;
      }

      this.filePath = Path.of(dir, file).toString();
      init();
    });

    // Labels toggle
    this.showCityLabels = new JCheckBox("Show cities labels");
    showCityLabels.setSelected(false);
    showCityLabels.addItemListener(e -> {
      displayArea.setShowLabels(showCityLabels.isSelected());
    });

    // Heuristic choice label
    this.heuristicChoiceLabel = new JLabel("Heuristic:");

    // Heuristic choice
    this.heuristicChoice = new JComboBox<>(heuristics);
    heuristicChoice.setEnabled(false);

    // Start city index choice label
    this.startCityIndexChoiceLabel = new JLabel("Start city index:");

    // Start city index choice
    this.startCityIndexChoice = new JComboBox<>();
    this.startCityIndexChoice.setEnabled(false);

    // Speed choice label
    this.speedChoiceLabel = new JLabel("Animation speed:");
    this.speedChoiceLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

    // Speed choice
    String[] speeds = {"Instantaneous", "Ludicrous", "Slow", "Slower", "Snail"};
    this.speedChoice = new JComboBox<>(speeds);
    speedChoice.setSelectedIndex(2);
    speedChoice.setEnabled(false);
    speedChoice.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String selectedSpeed = (String) e.getItem();
        delay = switch (selectedSpeed) {
          case "Instantaneous" -> 0;
          case "Ludicrous" -> 5;
          case "Slower" -> 300;
          case "Snail" -> 800;
          default -> 100;
        };

        if (animation != null)
          animation.setDelay(delay);
      }
    });

    // Run continuously button
    this.runContinuouslyButton = new JButton("Run continuously");
    runContinuouslyButton.setEnabled(false);
    runContinuouslyButton.addActionListener(e -> {
      this.runContinuously();
    });

    // Stop button
    this.stopRunButton = new JButton("Stop");
    stopRunButton.setEnabled(false);
    stopRunButton.addActionListener(e -> {
      this.stop();
    });

    // Run one step button
    this.runOneStepButton = new JButton("Run one step");
    runOneStepButton.setEnabled(false);
    runOneStepButton.addActionListener(e -> {
      this.initHeuristic();
      animation.doStep();
    });

    // Reset button
    this.resetButton = new JButton("Reset");
    resetButton.setEnabled(false);
    resetButton.addActionListener(e -> {
      this.reset(false);
    });

    // Results label
    this.resultsLabel = new JLabel("Results: Awaiting execution");

    ////////////////////////////////////////////////////////////
    // Configuring the options bar & adding all the components
    ////////////////////////////////////////////////////////////
    this.optionsPanel.setLayout(new GridLayout(2, 1));
    this.topOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    this.bottomOptionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    this.optionsPanel.add(this.topOptionsPanel);
    this.optionsPanel.add(this.bottomOptionsPanel);

    // File picker & current file
    this.topOptionsPanel.add(showFilePickerButton);
    this.topOptionsPanel.add(this.currentFileLabel);
    this.topOptionsPanel.add(this.showCityLabels);
    this.topOptionsPanel.add(this.createVerticalSeparator());

    // Optimization options
    this.topOptionsPanel.add(this.heuristicChoiceLabel);
    this.topOptionsPanel.add(this.heuristicChoice);
    this.topOptionsPanel.add(this.startCityIndexChoiceLabel);
    this.topOptionsPanel.add(this.startCityIndexChoice);

    // Run & results
    this.bottomOptionsPanel.add(this.speedChoiceLabel);
    this.bottomOptionsPanel.add(this.speedChoice);
    this.bottomOptionsPanel.add(this.runContinuouslyButton);
    this.bottomOptionsPanel.add(this.stopRunButton);
    this.bottomOptionsPanel.add(this.createVerticalSeparator());
    this.bottomOptionsPanel.add(this.runOneStepButton);
    this.bottomOptionsPanel.add(this.resetButton);
    this.bottomOptionsPanel.add(this.createVerticalSeparator());
    this.bottomOptionsPanel.add(this.resultsLabel);

    ////////////////////////////////////////////////////////////
    // Showing everything
    ////////////////////////////////////////////////////////////
    this.window.add(this.optionsPanel, BorderLayout.NORTH);
    this.window.add(displayArea, BorderLayout.CENTER);
    this.displayArea.setVisible(true);
    this.optionsPanel.setVisible(true);
    this.topOptionsPanel.setVisible(true);
    this.bottomOptionsPanel.setVisible(true);
    this.window.setVisible(true);
  }

  /**
   * Creates a vertical separator.
   *
   * @return A vertical JSeparator.
   */
  private JComponent createVerticalSeparator() {
    JSeparator x = new JSeparator(SwingConstants.VERTICAL);
    x.setPreferredSize(new Dimension(3, TspSolverGui.TOPBAR_HEIGHT - 10));
    return x;
  }

  /**
   * Inits the animation with the chosen heuristic.
   */
  private void initAnimation() {
    // Instantiating the correct heuristic
    Object chosenHeuristicItem = this.heuristicChoice.getSelectedItem();
    if (chosenHeuristicItem == null) {
      JOptionPane.showMessageDialog(
          this.window,
          "An unknown error happened",
          "Error",
          JOptionPane.ERROR_MESSAGE
      );
      return;
    }

    ObservableTspConstructiveHeuristic chosenHeuristic = (ObservableTspConstructiveHeuristic) chosenHeuristicItem;

    Object startCityChosenItem = this.startCityIndexChoice.getSelectedItem();
    if (startCityChosenItem == null) {
      JOptionPane.showMessageDialog(
          this.window,
          "An unknown error happened",
          "Error",
          JOptionPane.ERROR_MESSAGE
      );
      return;
    }

    int startCity = (Integer) startCityChosenItem == -1
        ? RND.nextInt(currentData.getNumberOfCities())
        : (Integer) startCityChosenItem;

    // Disabling heuristic choice related components
    this.heuristicChoice.setEnabled(false);
    this.startCityIndexChoice.setEnabled(false);

    // Enabling the reset button
    this.resetButton.setEnabled(true);

    animation = new Animation(displayArea, delay);
    Supplier<TspTour> callable = () -> chosenHeuristic.computeTour(currentData, startCity, animation);

    CompletableFuture.supplyAsync(callable, EXECUTOR_SERVICE)
        .whenComplete((tour, throwable) -> {
          if (throwable != null && !(throwable.getCause() instanceof CancelledAnimationException)) {
            throwable.printStackTrace();
            JOptionPane.showMessageDialog(
                this.window,
                "An error happened while building the tour.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            reset(false);
          }

          heuristicComplete(tour);
        });
  }

  /**
   * Does the clean-up and post-heuristic actions.
   */
  private void heuristicComplete(TspTour tspTour) {
    int[] tour = tspTour.tour();
    var it = IntStream.range(0, tour.length)
        .mapToObj(i -> new Edge(tour[i], tour[(i + 1) % tour.length]))
        .iterator();
    displayArea.update(it);
    this.resultsLabel.setText("Done. Distance is " + tspTour.length());
    this.reset(true);
  }

  /**
   * Does some initialisation work when starting the heuristic.
   */
  private void initHeuristic() {
    if (this.animation == null)
      this.initAnimation();

    // Updating the status label
    this.resultsLabel.setText("Distance: Computing...");
  }

  /**
   * Starts (or restarts) a continuous run.
   */
  private void runContinuously() {
    this.initHeuristic();

    // Disabling the 2 runs buttons
    this.runContinuouslyButton.setEnabled(false);
    this.runOneStepButton.setEnabled(false);

    // Enabling the stop button
    this.stopRunButton.setEnabled(true);

    animation.resume();
  }

  /**
   * Stops a continuous execution.
   */
  private void stop() {
    animation.pause();

    // Enabling the 2 runs buttons
    this.runContinuouslyButton.setEnabled(true);
    this.runOneStepButton.setEnabled(true);

    // Disabling the run button
    this.stopRunButton.setEnabled(false);
  }

  /**
   * Resets the state.
   *
   * @param heuristicComplete Whether it's a reset in the middle of a heuristic or at the end of one.
   */
  private void reset(boolean heuristicComplete) {
    if (animation != null) {
      animation.cancel();
      animation = null;
    }

    // Enabling the heuristic choice component
    this.heuristicChoice.setEnabled(true);

    // Enabling the speed & start city components
    this.startCityIndexChoice.setEnabled(true);
    this.speedChoice.setEnabled(true);

    // Enabling the run buttons
    this.runContinuouslyButton.setEnabled(true);
    this.runOneStepButton.setEnabled(true);

    // Disabling the reset button
    this.resetButton.setEnabled(false);

    // Disabling the stop button
    this.stopRunButton.setEnabled(false);

    // Updating results label
    if (!heuristicComplete) {
      this.resultsLabel.setText("Results: Awaiting execution");
      displayArea.reset(currentData);
    }
  }

  /**
   * Initialises the data by loading the file.
   */
  private void init() {
    try {
      // TSP Solving data
      currentData = TspData.fromFile(this.filePath);
      this.currentFileLabel.setText("Current file: " + this.dataFileDialog.getFile());
      this.startCityIndexChoice.removeAllItems();
      this.startCityIndexChoice.addItem(-1);
      for (int i = 0; i < currentData.getNumberOfCities(); ++i) {
        this.startCityIndexChoice.addItem(i);
      }
      this.startCityIndexChoice.setSelectedIndex(0);

      reset(false);
    } catch (TspParsingException | FileNotFoundException e) {
      JOptionPane.showMessageDialog(
          this.window,
          "Error while parsing the TSP Data",
          "File error",
          JOptionPane.ERROR_MESSAGE
      );
    }
  }
}
