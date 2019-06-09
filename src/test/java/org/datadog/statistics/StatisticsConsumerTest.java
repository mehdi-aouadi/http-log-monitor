package org.datadog.statistics;

import com.google.common.eventbus.EventBus;
import org.datadog.log.CommonLogFormatEntry;
import org.datadog.statitics.StatisticsConsumer;
import org.datadog.statitics.TrafficStatistic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsConsumerTest {

  @Mock
  private EventBus eventBus;

  @Test
  public void refreshStatisticsNominalTest() {
    StatisticsConsumer statisticsConsumer = new StatisticsConsumer(eventBus, 10);
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
    statisticsConsumer.consumeClfEvent(commonLogFormatEntry);
    statisticsConsumer.refreshStatistics(30);

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
    statisticsConsumer.consumeClfEvent(commonLogFormatEntry);

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
    statisticsConsumer.consumeClfEvent(commonLogFormatEntry);

    statisticsConsumer.refreshStatistics(30);

    trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 2);
        }})
        .build();

    Mockito.verify(eventBus).post(Mockito.eq(trafficStatistic));
  }

  @Test
  public void refreshStatisticsBadSectionDiscardTest() {
    StatisticsConsumer statisticsConsumer = new StatisticsConsumer(eventBus, 10);
    CommonLogFormatEntry commonLogFormatEntry = CommonLogFormatEntry.builder()
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
    statisticsConsumer.consumeClfEvent(commonLogFormatEntry);

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
    statisticsConsumer.consumeClfEvent(commonLogFormatEntry);

    statisticsConsumer.refreshStatistics(30);

    TrafficStatistic trafficStatistic = TrafficStatistic.builder()
        .totalTrafficSize(300)
        .sectionsHits(new HashMap<String, Integer>() {{
          put("pages", 1);
        }})
        .build();

    Mockito.verify(eventBus).post(Mockito.eq(trafficStatistic));
  }


}
