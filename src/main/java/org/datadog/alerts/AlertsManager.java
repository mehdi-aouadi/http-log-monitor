package org.datadog.alerts;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.google.inject.Inject;

import java.time.ZonedDateTime;

import lombok.NonNull;

import lombok.extern.slf4j.Slf4j;
import org.datadog.statitics.TrafficStatistic;

@Slf4j
public class AlertsManager {

  private final EventBus eventBus;
  private final EvictingQueue<TrafficStatistic> trafficStatisticsQueue;
  private final int hitsThreshold;

  private int totalHits = 0;
  private boolean highTrafficTriggered = false;

  /**
   * Creates a new {@link AlertsManager}.
   * @param eventBus The {@link EventBus} used to publish {@link TrafficAlert}.
   * @param maxTrafficStatistics The maximum number of {@link TrafficStatistic}
   *                             to hold in the buffer. It is equal to the alerts monitoring
   *                             internal divided by the traffic statistics computation
   *                             interval.
   * @param hitsThreshold The max hits by second threshold to trigger a new {@link TrafficAlert}.
   */
  @Inject
  public AlertsManager(@NonNull EventBus eventBus,
                       int maxTrafficStatistics,
                       int hitsThreshold) {

    Preconditions.checkArgument(maxTrafficStatistics > 0, "Max Traffic Statistics must be > 0.");
    Preconditions.checkArgument(hitsThreshold > 0, "Hits Threshold must be > 0.");

    this.eventBus = eventBus;
    this.trafficStatisticsQueue = EvictingQueue.create(maxTrafficStatistics);
    this.hitsThreshold = hitsThreshold;
  }

  /**
   * Checks if a {@link TrafficAlert} should be published.
   * @param trafficStatistic The last last received {@link TrafficStatistic}.
   */
  private void checkForAlert(TrafficStatistic trafficStatistic) {

    if (this.trafficStatisticsQueue.remainingCapacity() == 0) {
      this.totalHits -= this.trafficStatisticsQueue.peek().getTotalHitsCount();
    }

    this.trafficStatisticsQueue.add(trafficStatistic);

    this.totalHits += trafficStatistic.getTotalHitsCount();

    float hitsAverage = (float) this.totalHits / this.trafficStatisticsQueue.size();
    if (this.highTrafficTriggered && hitsAverage < this.hitsThreshold) {
      this.highTrafficTriggered = false;
      this.eventBus.post(
          TrafficAlert.builder()
          .alertType(TrafficAlert.AlertType.RECOVERED_TRAFFIC)
          .alertDateTime(ZonedDateTime.now())
          .hitsAverage(hitsAverage)
          .build()
      );
    } else if (!this.highTrafficTriggered && hitsAverage >= this.hitsThreshold) {
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
  void consumeTrafficStatistics(TrafficStatistic trafficStatistic) {
    this.checkForAlert(trafficStatistic);
  }

}
