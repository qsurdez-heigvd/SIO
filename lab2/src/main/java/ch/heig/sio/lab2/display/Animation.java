package ch.heig.sio.lab2.display;

import ch.heig.sio.lab2.tsp.Edge;

import javax.swing.*;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

final class Animation implements TspHeuristicObserver {
  private enum Command {
    DISPLAY,
    CANCEL,
    PASS
  }

  private final TspDisplayArea displayArea;
  private int delay;
  private Timer timer;
  private final BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<>(2);

  Animation(TspDisplayArea displayArea, int delay) {
    this.displayArea = displayArea;
    setDelay(delay);
  }

  @Override
  public void update(Iterator<Edge> edges) {
    Command cmd;
    try {
      cmd = commandQueue.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }

    switch (cmd) {
      case DISPLAY:
        displayArea.update(edges);
        break;
      case CANCEL:
        throw new CancelledAnimationException();
      case PASS:
        // Hacky way of finishing the heuristic without animation
        commandQueue.offer(Command.PASS);
        break;
    }
  }

  public void cancel() {
    timer.stop();
    commandQueue.offer(Command.CANCEL);
  }

  public void resume() {
    if (delay <= 0) {
      commandQueue.offer(Command.PASS);
    } else {
      timer.start();
    }
  }

  public void pause() {
    timer.stop();
  }

  public void doStep() {
    commandQueue.offer(Command.DISPLAY);
  }

  public void setDelay(int delay) {
    this.delay = delay;
    if (timer == null) {
      timer = new Timer(delay, e -> commandQueue.offer(Command.DISPLAY));
      timer.setRepeats(true);
      timer.setInitialDelay(0);
    }

    if (delay <= 0) {
      if (timer.isRunning()) {
        timer.stop();
        commandQueue.offer(Command.PASS);
      }
    } else {
      timer.setDelay(delay);
    }
  }
}
