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
import org.datadog.statitics.TrafficStatistic;


public class ConsoleGui {

  private final MonitoringWindow monitoringWindow = new MonitoringWindow();

  @Subscribe
  public void handleTrafficStatistics(TrafficStatistic trafficStatistic) {
    monitoringWindow.handleTrafficStatistics(trafficStatistic);
  }

  @Subscribe
  public void handleTrafficAlert(TrafficAlert trafficAlert) {
    monitoringWindow.handleTrafficAlert(trafficAlert);
  }

  public void start(Runnable exitCallback) throws IOException {
    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    try (Screen screen = new TerminalScreen(terminal)) {
      screen.setCursorPosition(null);
      screen.startScreen();
      terminal.addResizeListener((terminal1, terminalSize)
          -> monitoringWindow.onTerminalResize(terminalSize));
      monitoringWindow.onTerminalResize(screen.getTerminalSize());
      MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(),
          null, new EmptySpace(TextColor.ANSI.BLACK));
      gui.addWindowAndWait(monitoringWindow);
      exitCallback.run();
    }
  }


}
