package org.datadog.statitics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.time.Instant;
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
public class StatisticsConsumer {

  private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsConsumer.class);

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
  public StatisticsConsumer(EventBus eventBus, int refreshPeriod) {
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
    Instant test = commonLogFormatEntry.getLogDateTime().toInstant();
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
    this.eventBus.post(
        TrafficStatistic.builder().totalTrafficSize(trafficSize).sectionsHits(sectionsHits).build()
    );
  }

  @Subscribe
  public void consumeClfEvent(CommonLogFormatEntry commonLogFormatEntry) {
    this.logStore.add(commonLogFormatEntry);
  }

}
