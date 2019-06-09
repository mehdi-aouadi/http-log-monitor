package org.datadog.statistics;

import com.google.common.eventbus.EventBus;
import org.datadog.log.CommonLogFormatEntry;
import org.datadog.statitics.StatisticsManager;
import org.datadog.statitics.TrafficStatistic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsManagerTest {

  @Mock
  private EventBus eventBus;

  @Test
  public void refreshStatisticsNominalTest() {
    StatisticsManager statisticsManager = new StatisticsManager(eventBus, 10);
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
    statisticsManager.consumeClfEvent(commonLogFormatEntry);
    statisticsManager.refreshStatistics(30);

    TrafficStatistic trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(123)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 1);
        }})
        .build();

    Mockito.verify(eventBus).post(Mockito.eq(trafficStatistic));

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
    statisticsManager.consumeClfEvent(commonLogFormatEntry);

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
    statisticsManager.consumeClfEvent(commonLogFormatEntry);

    statisticsManager.refreshStatistics(30);

    trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 2);
        }})
        .build();

    Mockito.verify(eventBus).post(Mockito.eq(trafficStatistic));

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
    statisticsManager.consumeClfEvent(commonLogFormatEntry);

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
    statisticsManager.consumeClfEvent(commonLogFormatEntry);

    statisticsManager.refreshStatistics(30);

    trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 1);
        }})
        .build();

    Mockito.verify(eventBus).post(Mockito.eq(trafficStatistic));
  }


}
