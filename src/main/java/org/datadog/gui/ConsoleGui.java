package org.datadog.gui;

import com.google.common.eventbus.Subscribe;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import org.datadog.alerts.TrafficAlert;
import org.datadog.cli.ApplicationOptions;
import org.datadog.statitics.TrafficStatistic;


public class ConsoleGui {

  private final MonitoringWindow monitoringWindow;

  public ConsoleGui(ApplicationOptions applicationOptions) {
    this.monitoringWindow = new MonitoringWindow(applicationOptions);
  }
  @Subscribe
  public void handleTrafficStatistics(TrafficStatistic trafficStatistic) {
    this.monitoringWindow.handleTrafficStatistics(trafficStatistic);
  }

  @Subscribe
  public void handleTrafficAlert(TrafficAlert trafficAlert) {
    this.monitoringWindow.handleTrafficAlert(trafficAlert);
  }

  /**
   * Runs the Console GUI.
   * @param exitCallback the {@link Runnable} to execute when when closing the Console Gui.
   * @throws IOException If there was an underlying I/O error when querying the size of the
   *     terminal.
   */
  public void start(Runnable exitCallback) throws IOException {
    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    try (Screen screen = new TerminalScreen(terminal)) {
      screen.setCursorPosition(null);
      screen.startScreen();
      terminal.addResizeListener((terminal1, terminalSize)
          -> this.monitoringWindow.onTerminalResize(terminalSize));
      this.monitoringWindow.onTerminalResize(screen.getTerminalSize());
      MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
          null, new EmptySpace(TextColor.ANSI.BLACK));
      gui.addWindowAndWait(this.monitoringWindow);
      exitCallback.run();
    }
  }


}
