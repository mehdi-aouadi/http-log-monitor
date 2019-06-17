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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

import org.datadog.alerts.TrafficAlert;
import org.datadog.cli.ApplicationOptions;
import org.datadog.statitics.TrafficStatistic;
import org.datadog.utils.GuiFormatUtils;

import static java.util.Map.Entry.comparingByValue;
import static org.datadog.utils.DateTimeUtils.toPrettyDuration;

class MonitoringWindow extends BasicWindow {

  private final Panel settingsPanel = new Panel();
  private final Panel trafficStatisticsPanel = new Panel();
  private final Panel trafficAlertsPanel = new Panel();
  private final Panel optionsPanel = new Panel();
  private final Panel firstColumnSettingsPanel = new Panel();
  private final Panel secondColumnSettingsPanel = new Panel();

  private final EvictingQueue<TrafficAlert> trafficAlertsBuffer = EvictingQueue.create(50);

  private final Instant monitoringStartingTime = Instant.now();

  private Label startedTimeLabel = new Label("Monitoring started "
      + toPrettyDuration(monitoringStartingTime, Instant.now()) + " ago.");

  MonitoringWindow(ApplicationOptions applicationOptions) {
    settingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    optionsPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
    firstColumnSettingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    firstColumnSettingsPanel.addComponent(
        new Label("Refresh every : " + applicationOptions.getRefreshFrequency() + " s")
    );
    firstColumnSettingsPanel.addComponent(
        new Label("Hits average threshold : " + applicationOptions.getTrafficThreshold())
    );
    optionsPanel.addComponent(firstColumnSettingsPanel);
    secondColumnSettingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    secondColumnSettingsPanel.addComponent(
        new Label("File path : " + applicationOptions.getFilePath())
    );
    secondColumnSettingsPanel.addComponent(
        new Label("Check threshold every : "
            + applicationOptions.getRefreshFrequency() * applicationOptions.getRefreshFrequency())
    );
    optionsPanel.addComponent(secondColumnSettingsPanel);
    settingsPanel.addComponent(optionsPanel);
    settingsPanel.addComponent(startedTimeLabel);
    trafficStatisticsPanel.setLayoutManager(new LinearLayout());
    trafficAlertsPanel.setLayoutManager(new LinearLayout());
    Panel monitoringPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
    monitoringPanel.addComponent(
        trafficStatisticsPanel.withBorder(Borders.singleLine("Traffic Statistics"))
    );
    monitoringPanel.addComponent(
        trafficAlertsPanel.withBorder(Borders.singleLine("Traffic Alerts"))
    );
    Panel rootPanel = new Panel(new LinearLayout(Direction.VERTICAL));
    rootPanel.addComponent(
        settingsPanel.withBorder(Borders.singleLine("Monitoring Settings"))
    );
    rootPanel.addComponent(monitoringPanel);
    Panel statusPanel = new Panel();
    rootPanel.addComponent(statusPanel);
    setComponent(rootPanel);
    setHints(Arrays.asList(Hint.FULL_SCREEN, Hint.NO_DECORATIONS));
  }

  void onTerminalResize(TerminalSize terminalSize) {
    TerminalSize settingsSection = new TerminalSize(
        terminalSize.getColumns(),
        terminalSize.getRows() / 8
    );
    TerminalSize columnSettingsSection = new TerminalSize(
        terminalSize.getColumns() / 2,
        terminalSize.getRows() / 9
    );

    firstColumnSettingsPanel.setPreferredSize(columnSettingsSection);
    secondColumnSettingsPanel.setPreferredSize(columnSettingsSection);
    settingsPanel.setPreferredSize(settingsSection);

    TerminalSize monitoringSections = new TerminalSize(
        terminalSize.getColumns() / 2,
        terminalSize.getRows() * 7 / 8
    );

    trafficStatisticsPanel.setPreferredSize(monitoringSections);
    trafficAlertsPanel.setPreferredSize(monitoringSections);
  }

  void handleTrafficStatistics(TrafficStatistic trafficStatistic) {
    this.settingsPanel.removeComponent(startedTimeLabel);
    this.startedTimeLabel = new Label("Monitoring started "
        + toPrettyDuration(monitoringStartingTime, Instant.now()) + " ago.");
    this.settingsPanel.addComponent(startedTimeLabel);
    trafficStatisticsPanel.removeAllComponents();
    trafficStatisticsPanel.addComponent(new Label("\nSummary").addStyle(SGR.BOLD));
    trafficStatisticsPanel.addComponent(new Label("Total Requests: "
        + trafficStatistic.getTotalHitsCount()));
    trafficStatisticsPanel.addComponent(new Label("Success Requests: "
        + trafficStatistic.getSuccessRequestsCount()).setForegroundColor(
            TextColor.Factory.fromString("#003c00"))
    );
    trafficStatisticsPanel.addComponent(new Label("Client Error requests: "
        + trafficStatistic.getClientErrorRequestCount()).setForegroundColor(
        TextColor.Factory.fromString("#5e0000"))
    );
    trafficStatisticsPanel.addComponent(new Label("Server Error requests: "
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
