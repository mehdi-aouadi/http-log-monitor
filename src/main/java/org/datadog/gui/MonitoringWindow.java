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
  private Label startedTimeLabel = new Label("Monitoring started 0s ago.");

  private final EvictingQueue<TrafficAlert> trafficAlertsBuffer = EvictingQueue.create(50);

  private final Instant monitoringStartingTime = Instant.now();

  private Instant lastRefresh = Instant.now();

  private final ApplicationOptions applicationOptions;

  MonitoringWindow(ApplicationOptions applicationOptions) {
    this.applicationOptions = applicationOptions;
    this.settingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    this.optionsPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
    this.firstColumnSettingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    this.firstColumnSettingsPanel.addComponent(
        new Label("Refresh every : " + this.applicationOptions.getRefreshFrequency() + " s")
    );
    this.firstColumnSettingsPanel.addComponent(
        new Label("Hits average threshold : " + applicationOptions.getTrafficThreshold())
    );
    this.optionsPanel.addComponent(this.firstColumnSettingsPanel);
    this.secondColumnSettingsPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
    this.secondColumnSettingsPanel.addComponent(
        new Label("File path : " + applicationOptions.getFilePath())
    );
    this.secondColumnSettingsPanel.addComponent(
        new Label("Check hits average for the last : "
            + (applicationOptions.getRefreshFrequency()
            * applicationOptions.getThresholdRefreshCycles()
            + " s")
        )
    );
    this.optionsPanel.addComponent(this.secondColumnSettingsPanel);
    this.settingsPanel.addComponent(this.optionsPanel);
    this.settingsPanel.addComponent(this.startedTimeLabel);
    this.trafficStatisticsPanel.setLayoutManager(new LinearLayout());
    this.trafficAlertsPanel.setLayoutManager(new LinearLayout());
    Panel monitoringPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
    monitoringPanel.addComponent(
        this.trafficStatisticsPanel.withBorder(Borders.singleLine("Traffic Statistics"))
    );
    monitoringPanel.addComponent(
        this.trafficAlertsPanel.withBorder(Borders.singleLine("Traffic Alerts"))
    );
    Panel rootPanel = new Panel(new LinearLayout(Direction.VERTICAL));
    rootPanel.addComponent(
        this.settingsPanel.withBorder(Borders.singleLine("Monitoring Settings"))
    );
    Panel quitMessagePanel = new Panel();
    quitMessagePanel.addComponent(new Label("Press '^C' or click on 'X' to quit."));
    rootPanel.addComponent(monitoringPanel);
    Panel statusPanel = new Panel();
    rootPanel.addComponent(statusPanel);
    rootPanel.addComponent(quitMessagePanel);
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
        terminalSize.getRows() / 10
    );

    this.firstColumnSettingsPanel.setPreferredSize(columnSettingsSection);
    this.secondColumnSettingsPanel.setPreferredSize(columnSettingsSection);
    this.settingsPanel.setPreferredSize(settingsSection);

    TerminalSize monitoringSections = new TerminalSize(
        terminalSize.getColumns() / 2,
        terminalSize.getRows() * 6 / 8
    );

    this.trafficStatisticsPanel.setPreferredSize(monitoringSections);
    this.trafficAlertsPanel.setPreferredSize(monitoringSections);
  }

  void handleTrafficStatistics(TrafficStatistic trafficStatistic) {
    this.lastRefresh = this.lastRefresh.plusSeconds(this.applicationOptions.getRefreshFrequency());
    this.settingsPanel.removeComponent(this.startedTimeLabel);
    this.startedTimeLabel = new Label("Monitoring started "
        + toPrettyDuration(
        this.monitoringStartingTime,
        this.lastRefresh) + " ago."
    );
    this.settingsPanel.addComponent(this.startedTimeLabel);
    this.trafficStatisticsPanel.removeAllComponents();
    this.trafficStatisticsPanel.addComponent(new Label("\nSummary").addStyle(SGR.BOLD));
    this.trafficStatisticsPanel.addComponent(new Label("Total Requests: "
        + trafficStatistic.getTotalHitsCount()));
    this.trafficStatisticsPanel.addComponent(new Label("Success Requests: "
        + trafficStatistic.getSuccessRequestsCount()).setForegroundColor(
        TextColor.Factory.fromString("#003c00"))
    );
    this.trafficStatisticsPanel.addComponent(new Label("Client Error requests: "
        + trafficStatistic.getClientErrorRequestCount()).setForegroundColor(
        TextColor.Factory.fromString("#5e0000"))
    );
    this.trafficStatisticsPanel.addComponent(new Label("Server Error requests: "
        + trafficStatistic.getServerErrorRequestCount()).setForegroundColor(
        TextColor.Factory.fromString("#cc0000"))
    );
    this.trafficStatisticsPanel.addComponent(new Label("Total traffic size: "
        + GuiFormatUtils.humanReadableByteCount(trafficStatistic.getTotalTrafficSize())));
    this.trafficStatisticsPanel.addComponent(new Label("\nHits By Section").addStyle(SGR.BOLD));
    trafficStatistic.getSectionsHits()
        .forEach(entry ->
            this.trafficStatisticsPanel.addComponent(
                new Label(entry.getKey() + " " + entry.getValue())
            )
      );
    this.trafficStatisticsPanel.addComponent(new Label("\nHits By Method").addStyle(SGR.BOLD));
    trafficStatistic.getMethodsHits().entrySet().stream()
        .sorted(Collections.reverseOrder(comparingByValue()))
        .forEach(entry ->
            this.trafficStatisticsPanel.addComponent(
                new Label(entry.getKey() + " " + entry.getValue())
            )
    );
  }

  void handleTrafficAlert(TrafficAlert alert) {
    this.trafficAlertsBuffer.add(alert);
    this.trafficAlertsPanel.removeAllComponents();
    for (TrafficAlert trafficAlert : this.trafficAlertsBuffer) {
      Label label = new Label(trafficAlert.getMessage()).addStyle(SGR.BOLD);
      if (trafficAlert.getAlertType() == TrafficAlert.AlertType.HIGH_TRAFFIC) {
        label.setForegroundColor(TextColor.Factory.fromString("#5e0000"));
      } else {
        label.setForegroundColor(TextColor.Factory.fromString("#003c00"));
      }
      label.setText(label.getText() + "\n ");
      this.trafficAlertsPanel.addComponent(label);
    }
  }

}
