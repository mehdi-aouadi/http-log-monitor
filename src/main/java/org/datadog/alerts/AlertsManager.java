package org.datadog.alerts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.EvictingQueue;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.google.inject.Inject;

import java.time.ZonedDateTime;

import org.datadog.statitics.TrafficStatistic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlertsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertsManager.class);

  private final EventBus eventBus;
  private final EvictingQueue<TrafficStatistic> trafficStatisticsQueue;
  private final int hitsTreshold;
  private final int alertMonitoringInterval;

  private int totalHits = 0;
  private boolean highTrafficTriggered = false;

  /**
   * Creates a new {@link AlertsManager}.
   * @param eventBus The {@link EventBus} used to publish {@link TrafficAlert}.
   * @param maxTrafficStatistics The maximum number of {@link TrafficStatistic}
   *                            to hold in the buffer.
   * @param hitsThreshold The max hits by second threshold to trigger a new {@link TrafficAlert}.
   * @param alertMonitoringInterval The number of seconds after which the hits average
   *                               is calculated.
   */
  @Inject
  public AlertsManager(EventBus eventBus,
                       int maxTrafficStatistics,
                       int hitsThreshold,
                       int alertMonitoringInterval) {
    this.eventBus = eventBus;
    this.trafficStatisticsQueue = new EvictingQueue<>(maxTrafficStatistics);
    this.hitsTreshold = hitsThreshold;
    this.alertMonitoringInterval = alertMonitoringInterval;
  }

  /**
   * Checks if a {@link TrafficAlert} should be published.
   * @param trafficStatistic The last last received {@link TrafficStatistic}.
   */
  @VisibleForTesting
  public void checkForAlert(TrafficStatistic trafficStatistic) {

    if (this.trafficStatisticsQueue.remainingCapacity() == 0) {
      this.totalHits -= this.trafficStatisticsQueue.peek().getSectionsHits()
          .values().stream().mapToInt(Integer::intValue).sum();
    } else {
      this.trafficStatisticsQueue.add(trafficStatistic);
      this.totalHits += trafficStatistic.getSectionsHits()
          .values().stream().mapToInt(Integer::intValue).sum();
    }

    float hitsAverage = this.totalHits / alertMonitoringInterval;
    if (this.highTrafficTriggered && hitsTreshold < this.hitsTreshold) {
      this.eventBus.post(
          TrafficAlert.builder()
          .alertType(TrafficAlert.AlertType.RECOVERED_TRAFFIC)
          .alertDateTime(ZonedDateTime.now())
          .hitsAverage(hitsAverage)
          .build()
      );
    } else if (hitsAverage > this.hitsTreshold) {
      this.highTrafficTriggered = true;
      this.eventBus.post(
          TrafficAlert.builder()
              .alertType(TrafficAlert.AlertType.HIGH_TRAFFIC)
              .alertDateTime(ZonedDateTime.now())
              .hitsAverage(hitsAverage)
              .build()
      );
    }
  }

  @Subscribe
  public void consumeTraffixStatistics(TrafficStatistic trafficStatistic) {
    this.checkForAlert(trafficStatistic);
  }

}
