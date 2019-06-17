package org.datadog.gui;

import com.google.common.collect.EvictingQueue;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.datadog.alerts.TrafficAlert;
import org.datadog.statitics.TrafficStatistic;
import org.datadog.utils.GuiFormatUtils;

import static java.util.Map.Entry.comparingByValue;

class MonitoringWindow extends BasicWindow {

  private final Panel trafficStatisticsPanel = new Panel();
  private final Panel trafficAlertsPanel = new Panel();

  private final EvictingQueue<TrafficAlert> trafficAlertsBuffer = EvictingQueue.create(50);

  private final Instant monitoringStartingTime = Instant.now();

  MonitoringWindow() {
    trafficAlertsPanel.setLayoutManager(new LinearLayout());
    Panel mainPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
    mainPanel.addComponent(
        trafficStatisticsPanel.withBorder(Borders.singleLine("Traffic Statistics"))
    );
    mainPanel.addComponent(
        trafficAlertsPanel.withBorder(Borders.singleLine("Traffic Alerts"))
    );
    Panel rootPanel = new Panel();
    rootPanel.addComponent(mainPanel);
    Panel statusPanel = new Panel();
    rootPanel.addComponent(statusPanel);
    setComponent(rootPanel);
    setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));
  }

  void onTerminalResize(TerminalSize terminalSize) {
    TerminalSize half = new TerminalSize(terminalSize.getColumns() / 2, terminalSize.getRows() - 3);
    trafficStatisticsPanel.setPreferredSize(half);
    trafficAlertsPanel.setPreferredSize(half);
  }

  void handleTrafficStatistics(TrafficStatistic trafficStatistic) {
    trafficStatisticsPanel.removeAllComponents();
    trafficStatisticsPanel.addComponent(new Label("Monitoring started "
        + Duration.between(monitoringStartingTime, Instant.now()).withNanos(0).toString()
        .substring(2)
        .replaceAll("(\\d[HMS])(?!$)", "$1 ")
        .toLowerCase() + " ago."));
    trafficStatisticsPanel.addComponent(new Label("\nSummary").addStyle(SGR.BOLD));
    trafficStatisticsPanel.addComponent(new Label("Total Requests: "
        + trafficStatistic.getTotalHitsCount()));
    trafficStatisticsPanel.addComponent(new Label("Success Requests: "
        + trafficStatistic.getSuccessRequestsCount()).setForegroundColor(
            TextColor.Factory.fromString("#003c00"))
    );
    trafficStatisticsPanel.addComponent(new Label("Client Error request: "
        + trafficStatistic.getClientErrorRequestCount()).setForegroundColor(
        TextColor.Factory.fromString("#5e0000"))
    );
    trafficStatisticsPanel.addComponent(new Label("Server Error request: "
        + trafficStatistic.getServerErrorRequestCount()).setForegroundColor(
        TextColor.Factory.fromString("#cc0000"))
    );
    trafficStatisticsPanel.addComponent(new Label("Total Bytes Transferred: "
        + GuiFormatUtils.humanReadableByteCount(trafficStatistic.getTotalTrafficSize())));
    trafficStatisticsPanel.addComponent(new Label("\nHits By Section").addStyle(SGR.BOLD));
    trafficStatistic.getSectionsHits()
        .forEach(entry ->
            trafficStatisticsPanel.addComponent(
                new Label(entry.getKey() + " " + entry.getValue())
            )
      );
    trafficStatisticsPanel.addComponent(new Label("\nHits By Method").addStyle(SGR.BOLD));
    trafficStatistic.getMethodsHits().entrySet().stream()
        .sorted(Collections.reverseOrder(comparingByValue()))
        .forEach(entry ->
            trafficStatisticsPanel.addComponent(
                new Label(entry.getKey() + " " + entry.getValue())
            )
    );
  }

  void handleTrafficAlert(TrafficAlert alert) {
    trafficAlertsBuffer.add(alert);
    trafficAlertsPanel.removeAllComponents();
    for (TrafficAlert trafficAlert : trafficAlertsBuffer) {
      Label label = new Label(trafficAlert.getMessage()).addStyle(SGR.BOLD);
      if (trafficAlert.getAlertType() == TrafficAlert.AlertType.HIGH_TRAFFIC) {
        label.setForegroundColor(TextColor.Factory.fromString("#5e0000"));
      } else {
        label.setForegroundColor(TextColor.Factory.fromString("#003c00"));
      }
      label.setText(label.getText() + "\n ");
      trafficAlertsPanel.addComponent(label);
    }
  }

}
