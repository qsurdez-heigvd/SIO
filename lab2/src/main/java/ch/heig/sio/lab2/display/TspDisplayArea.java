package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.Edge;
import ch.heig.sio.lab2.tsp.TspData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A JPanel that displays a TSP instance and its tour.
 */
final class TspDisplayArea extends JPanel {
  /**
   * Listener for the resize event. Triggers its action only after the user has stopped resizing to avoid
   * unnecessary recalculations.
   */
  private final class OnResizeListener extends ComponentAdapter {
    private Timer resizeTimer = new Timer(0, null);

    @Override
    public void componentResized(ComponentEvent e) {
      resizeTimer.stop();

      resizeTimer = new Timer(50, e1 -> {
        cache();
        repaint();
      });

      resizeTimer.setRepeats(false);
      resizeTimer.start();
    }
  }

  // Data to display
  private TspData data;
  private ArrayList<Edge> tspTour;

  // Cached data
  private BufferedImage cityBuffer;
  private float zoomX;
  private float zoomY;
  private float offsetX;
  private float offsetY;

  // Display options
  private boolean showLabels;

  // Layout constants
  private static final int CITY_RADIUS = 5;
  private static final int LABEL_OFFSET = 8;
  private static final float MARGIN = 0.9f;

  TspDisplayArea() {
    super();
    addComponentListener(new OnResizeListener());
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // Prevent race conditions
    ArrayList<Edge> tour = tspTour;
    if (data == null || tour == null) {
      return;
    }

    Graphics2D g2d = (Graphics2D) g.create();
    configureGraphics(g2d);
    g2d.drawImage(cityBuffer, 0, 0, null);
    g2d.setStroke(new BasicStroke(2));
    g2d.setColor(Color.BLUE);

    for (Edge e : tour) {
      TspData.City city1 = data.getCityCoord(e.u());
      TspData.City city2 = data.getCityCoord(e.v());

      g2d.drawLine(
          (int) (city1.x() * zoomX + offsetX),
          (int) (offsetY - city1.y() * zoomY),
          (int) (city2.x() * zoomX + offsetX),
          (int) (offsetY - city2.y() * zoomY)
      );
    }

    g2d.dispose();
  }

  /**
   * Caches some data (mainly cities) to avoid recalculating it at each repaint.
   */
  private void cache() {
    if (data == null) {
      return;
    }

    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (int i = 0; i < data.getNumberOfCities(); ++i) {
      TspData.City coord = data.getCityCoord(i);
      int x = coord.x();
      int y = coord.y();

      if (x < minX)
        minX = x;

      if (x > maxX)
        maxX = x;

      if (y < minY)
        minY = y;

      if (y > maxY)
        maxY = y;
    }

    this.zoomX = ((float) getWidth() / (maxX - minX)) * MARGIN;
    this.zoomY = ((float) getHeight() / (maxY - minY)) * MARGIN;
    float marginX = getWidth() * (1 - MARGIN) / 2;
    float marginY = getHeight() * (1 - MARGIN) / 2;

    this.offsetX = (int) (-minX * zoomX + marginX);
    this.offsetY = (int) (getHeight() + minY * zoomY - marginY);

    cityBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = cityBuffer.createGraphics();
    configureGraphics(g2d);
    g2d.setBackground(Color.WHITE);
    g2d.clearRect(0, 0, getWidth(), getHeight());
    g2d.setColor(Color.RED);

    for (int i = 0; i < data.getNumberOfCities(); ++i) {
      TspData.City coord = data.getCityCoord(i);
      int x = (int) (coord.x() * zoomX + offsetX);
      int y = (int) (offsetY - coord.y() * zoomY);
      g2d.fillOval(
          x - CITY_RADIUS,
          y - CITY_RADIUS,
          CITY_RADIUS << 1,
          CITY_RADIUS << 1
      );

      if (showLabels)
        g2d.drawString(Integer.toString(i), x + LABEL_OFFSET, y - LABEL_OFFSET);
    }

    g2d.dispose();
  }

  private static void configureGraphics(Graphics2D g2d) {
    // Comment for better performance
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  /**
   * Updates the display with the new state of the tour.
   *
   * @param edges All edges composing the (incomplete) tour.
   */
  public void update(Iterator<Edge> edges) {
    tspTour = new ArrayList<>();
    edges.forEachRemaining(tspTour::add);
    repaint();
  }

  /**
   * Resets the display with the new data.
   *
   * @param data The new data to display.
   */
  public void reset(TspData data) {
    this.data = data;
    this.tspTour = new ArrayList<>();

    cache();
    repaint();
  }

  /**
   * Sets the display to show or hide the labels of the cities.
   *
   * @param showLabels True to show the labels, false to hide them.
   */
  public void setShowLabels(boolean showLabels) {
    this.showLabels = showLabels;
    cache();
    repaint();
  }
}
