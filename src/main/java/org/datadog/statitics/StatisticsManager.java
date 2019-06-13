package org.datadog.statitics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.datadog.log.CommonLogFormatEntry;
import org.datadog.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.datadog.utils.CommonLogFormatUtils.retrieveSection;

/**
 * Consumes the events of type {@link CommonLogFormatEntry} and generates {@link TrafficStatistic}.
 * The events are processed periodically according to the refresh period configuration.
 * The events are stored internally in {@link ConcurrentLinkedQueue}
 *  and retrieved from an {@link EventBus}
 */
public class StatisticsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsManager.class);

  private final EventBus eventBus;
  private final Queue<CommonLogFormatEntry> logStore = new ConcurrentLinkedQueue<>();

  /**
   * Creates a consumer of events of type {@link CommonLogFormatEntry}.
   * @param eventBus The {@link EventBus} used to listen to the events
   *                of type {@link CommonLogFormatEntry}
   * @param refreshPeriod The refresh period in seconds after which
   *                     the {@link CommonLogFormatEntry} events buffer is checked and all
   *                     the events of the last refresh periods seconds are processed.
   */
  @Inject
  public StatisticsManager(EventBus eventBus, int refreshPeriod) {
    this.eventBus = eventBus;
    TimerTask statisticRefreshTimerTask = new TimerTask() {
      @Override
      public void run() {
        refreshStatistics(refreshPeriod);
      }
    };
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(
        statisticRefreshTimerTask,
        0,
        refreshPeriod,
        TimeUnit.SECONDS
    );
  }

  /**
   * Processes all the {@link CommonLogFormatEntry} present in the buffer whose log date time
   *  is "timeSecondsInterval" seconds before now and produces a new {@link TrafficStatistic}.
   * @param timeSecondsInterval time interval second number.
   */
  @VisibleForTesting
  public void refreshStatistics(int timeSecondsInterval) {
    long trafficSize = 0;
    Map<String, Integer> sectionsHits = new HashMap<>();
    Instant maxAge = Instant.now().minusSeconds(timeSecondsInterval);
    CommonLogFormatEntry commonLogFormatEntry = logStore.poll();
    while (commonLogFormatEntry != null
        && commonLogFormatEntry.getLogDateTime().toInstant().isAfter(maxAge)) {
      trafficSize += commonLogFormatEntry.getSize();
      try {
        sectionsHits.merge(
            retrieveSection(commonLogFormatEntry.getResource()),
            1,
            Integer::sum
        );
      } catch (ParseException parseException) {
        LOGGER.error("Invalid resource url. Unable to retrieve section from {}."
                + " This request will not be considered in hits by section statistics.",
            commonLogFormatEntry.getResource(),
            parseException
        );
      }
      commonLogFormatEntry = this.logStore.poll();
    }

    if(!this.logStore.isEmpty()) {
      LOGGER.warn("Some Common Log Format Entry Events have not been processed and will be " +
              "discarded : {}",
          Arrays.asList(this.logStore));
      this.logStore.clear();
    }

    this.eventBus.post(
        TrafficStatistic.builder()
            .totalTrafficSize(trafficSize)
            .sectionsHits(sectionsHits)
            .build()
    );
  }

  /**
   * Consumes a {@link CommonLogFormatEntry} event.
   * It adds the {@link CommonLogFormatEntry} to the log buffer if its log date time is after
   *  the most recent stored log entry and discards it otherwise.
   * @param commonLogFormatEntry a {@link CommonLogFormatEntry} type event.
   */
  @Subscribe
  public void consumeClfEvent(CommonLogFormatEntry commonLogFormatEntry) {
    // The following check is made only for unusual behaviour.
    // According to Guava EventBus documentation the events are received in the same
    //  publishing order.
    // This check enforces a clean log store in case of corrupted log entries or unordered events
    // reception.
    CommonLogFormatEntry lastLogEntry = this.logStore.peek();
    if (lastLogEntry.getLogDateTime().isAfter(commonLogFormatEntry.getLogDateTime())) {
      LOGGER.warn(
          "Common Log Format Entry Event discarded : {}. "
              + "Log date time is before the last log entry in the buffer {}.",
          commonLogFormatEntry,
          lastLogEntry
      );
    } else {
      this.logStore.add(commonLogFormatEntry);
    }
  }

}
