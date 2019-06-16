package org.datadog.statitics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import org.datadog.log.CommonLogFormatEntry;
import org.datadog.parser.ParseException;
import org.datadog.utils.CommonLogFormatUtils;

import static org.datadog.utils.CommonLogFormatUtils.retrieveSection;

/**
 * Consumes the events of type {@link CommonLogFormatEntry} and generates {@link TrafficStatistic}.
 * The events are processed periodically according to the refresh period configuration.
 * The events are stored internally in {@link ConcurrentLinkedQueue}
 *  and retrieved from an {@link EventBus}
 */
@Slf4j
public class TrafficStatisticsManager {

  private final EventBus eventBus;
  private final Queue<CommonLogFormatEntry> logStore = new ConcurrentLinkedQueue<>();
  private Instant maxAge = Instant.now();

  /**
   * Creates a consumer of events of type {@link CommonLogFormatEntry}.
   * @param eventBus The {@link EventBus} used to listen to the events
   *                of type {@link CommonLogFormatEntry}
   * @param refreshPeriod The refresh period in seconds after which
   *                     the {@link CommonLogFormatEntry} events buffer is checked and all
   *                     the events of the last refresh periods seconds are processed.
   */
  @Inject
  public TrafficStatisticsManager(EventBus eventBus, int refreshPeriod) {
    this.eventBus = eventBus;
    Executor executor = Executors.newCachedThreadPool();
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        executor.execute(() -> {
          maxAge = Instant.now().plusSeconds(refreshPeriod);
          refreshStatistics(maxAge);
        });
      }
    }, 0, refreshPeriod * 1000);
  }

  /**
   * Processes all the {@link CommonLogFormatEntry} present in the buffer whose log date time
   *  is "timeSecondsInterval" seconds before now and produces a new {@link TrafficStatistic}.
   * @param maxAge the maximum log date time to process from the buffer.
   */
  @VisibleForTesting
  public void refreshStatistics(Instant maxAge) {
    long trafficSize = 0;
    int totalHits = 0;
    int successCount = 0;
    int clientErrorCount = 0;
    int serverErrorCount = 0;
    Map<String, Integer> sectionsHits = new HashMap<>();
    Map<String, Integer> methodsHits = new HashMap<>();
    CommonLogFormatEntry commonLogFormatEntry = logStore.poll();
    while (commonLogFormatEntry != null
        && !commonLogFormatEntry.getLogDateTime().toInstant().isAfter(maxAge)) {
      trafficSize += commonLogFormatEntry.getSize();
      totalHits++;
      if (commonLogFormatEntry.getStatus() >= 200
          && commonLogFormatEntry.getStatus() < 300) {
        successCount++;
      } else if (commonLogFormatEntry.getStatus() >= 400
          && commonLogFormatEntry.getStatus() < 500) {
        clientErrorCount++;
      } else if (commonLogFormatEntry.getStatus() >= 500
          && commonLogFormatEntry.getStatus() < 600) {
        serverErrorCount++;
      }

      try {
        sectionsHits.merge(
            retrieveSection(commonLogFormatEntry.getResource()),
            1,
            Integer::sum
        );
      } catch (ParseException parseException) {
        log.error("Invalid resource url. Unable to retrieve section from {}."
                + " This request will not be considered in hits by section statistics.",
            commonLogFormatEntry.getResource(),
            parseException
        );
      }

      methodsHits.merge(commonLogFormatEntry.getMethod(), 1, Integer::sum);

      commonLogFormatEntry = this.logStore.poll();
    }

    this.eventBus.post(
        TrafficStatistic.builder()
            .totalTrafficSize(trafficSize)
            .totalHitsCount(totalHits)
            .successRequestsCount(successCount)
            .clientErrorRequestCount(clientErrorCount)
            .serverErrorRequestCount(serverErrorCount)
            .sectionsHits(CommonLogFormatUtils.findGreatestValues(sectionsHits, 5))
            .methodsHits(methodsHits)
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
    if (lastLogEntry != null
        && commonLogFormatEntry.getLogDateTime().isBefore(lastLogEntry.getLogDateTime())
    ) {
      log.warn(
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
