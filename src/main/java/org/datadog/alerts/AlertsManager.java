package org.datadog.alerts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.EvictingQueue;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.datadog.statitics.TrafficStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertsManager.class);

  private final EventBus eventBus;
  private final EvictingQueue<TrafficStatistic> trafficStatisticsQueue;
  private final int hitsTreshold;

  @Inject
  public AlertsManager(EventBus eventBus, int maxTrafficStatistics, int hitsTreshold) {
    this.eventBus = eventBus;
    this.trafficStatisticsQueue = new EvictingQueue<>(maxTrafficStatistics);
    this.hitsTreshold = hitsTreshold;
  }

  @VisibleForTesting
  public void checkForAlert(TrafficStatistic trafficStatistic) {
    this.trafficStatisticsQueue.add(trafficStatistic);
  }

  @Subscribe
  public void consumeTraffixStatistics(TrafficStatistic trafficStatistic) {
    this.checkForAlert(trafficStatistic);
  }

}
