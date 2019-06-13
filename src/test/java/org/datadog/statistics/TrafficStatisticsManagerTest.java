package org.datadog.statistics;

import com.google.common.eventbus.EventBus;
import org.datadog.log.CommonLogFormatEntry;
import org.datadog.statitics.TrafficStatisticsManager;
import org.datadog.statitics.TrafficStatistic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.HashMap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

@RunWith(MockitoJUnitRunner.class)
public class TrafficStatisticsManagerTest {

  @Mock
  private EventBus eventBus;

  @Test
  public void refreshStatisticsNominalTest() {
    TrafficStatisticsManager trafficStatisticsManager = new TrafficStatisticsManager(eventBus, 10);
    CommonLogFormatEntry commonLogFormatEntry = CommonLogFormatEntry.builder()
        .host("localhost")
        .userRfcId("userRfcId")
        .userId("John Galt")
        .logDateTime(ZonedDateTime.now().minusSeconds(20))
        .method("GET")
        .resource("/pages/create")
        .protocol("HTTP")
        .status(200)
        .size(123)
        .build();
    trafficStatisticsManager.consumeClfEvent(commonLogFormatEntry);
    trafficStatisticsManager.refreshStatistics(30);

    TrafficStatistic trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(123)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 1);
        }})
        .build();

    verify(eventBus).post(eq(trafficStatistic));

    commonLogFormatEntry = CommonLogFormatEntry.builder()
        .host("localhost")
        .userRfcId("userRfcId")
        .userId("John Galt")
        .logDateTime(ZonedDateTime.now().minusSeconds(20))
        .method("GET")
        .resource("/pages/create")
        .protocol("HTTP")
        .status(200)
        .size(100)
        .build();
    trafficStatisticsManager.consumeClfEvent(commonLogFormatEntry);

    commonLogFormatEntry = CommonLogFormatEntry.builder()
        .host("localhost")
        .userRfcId("userRfcId")
        .userId("John Galt")
        .logDateTime(ZonedDateTime.now().minusSeconds(10))
        .method("GET")
        .resource("/pages/create")
        .protocol("HTTP")
        .status(200)
        .size(200)
        .build();
    trafficStatisticsManager.consumeClfEvent(commonLogFormatEntry);

    trafficStatisticsManager.refreshStatistics(30);

    trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 2);
        }})
        .build();

    verify(eventBus).post(eq(trafficStatistic));

    commonLogFormatEntry = CommonLogFormatEntry.builder()
        .host("localhost")
        .userRfcId("userRfcId")
        .userId("John Galt")
        .logDateTime(ZonedDateTime.now().minusSeconds(20))
        .method("GET")
        .resource("/pages")
        .protocol("HTTP")
        .status(200)
        .size(100)
        .build();
    trafficStatisticsManager.consumeClfEvent(commonLogFormatEntry);

    commonLogFormatEntry = CommonLogFormatEntry.builder()
        .host("localhost")
        .userRfcId("userRfcId")
        .userId("John Galt")
        .logDateTime(ZonedDateTime.now().minusSeconds(10))
        .method("GET")
        .resource("/pages/create")
        .protocol("HTTP")
        .status(200)
        .size(200)
        .build();
    trafficStatisticsManager.consumeClfEvent(commonLogFormatEntry);

    trafficStatisticsManager.refreshStatistics(30);

    trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 1);
        }})
        .build();

    verify(eventBus).post(eq(trafficStatistic));
  }


}
