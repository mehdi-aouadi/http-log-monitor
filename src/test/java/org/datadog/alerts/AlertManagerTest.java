package org.datadog.alerts;

import com.google.common.eventbus.EventBus;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.HashMap;

import org.datadog.statitics.TrafficStatistic;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AlertManagerTest {

  @Mock
  private EventBus eventBus;

  private AlertsManager alertsManagerUnderTest;
  private ZonedDateTime testExecutionDateTime;
  private final int maxTrafficStatistics = 4;
  private final int hitsThreshold = 10;

  @Before
  public void init() {
    reset(this.eventBus);
    this.alertsManagerUnderTest = new AlertsManager(this.eventBus, this.maxTrafficStatistics,
        this.hitsThreshold);
    testExecutionDateTime = ZonedDateTime.now();
  }

  @Test
  public void firstTrafficStatisticsAboveThreshold() {
    TrafficStatistic trafficStatistic = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 5);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(trafficStatistic);

    ArgumentCaptor<TrafficAlert> argumentCaptor = ArgumentCaptor.forClass(TrafficAlert.class);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals(trafficStatistic.totalHits(),
        argumentCaptor.getValue().getHitsAverage(),
        0);
    ZonedDateTime alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));
    assertEquals(MessageFormat.format(
        TrafficAlert.AlertType.HIGH_TRAFFIC.getMessage(),
        trafficStatistic.totalHits(),
        alertDateTime.toLocalTime().withNano(0)
    ), argumentCaptor.getValue().getMessage());
  }

  @Test
  public void noAlertsBeforeRecovering() {
    TrafficStatistic firstTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 5);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(firstTrafficStatistics);

    ArgumentCaptor<TrafficAlert> argumentCaptor = ArgumentCaptor.forClass(TrafficAlert.class);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals(firstTrafficStatistics.totalHits(),
        argumentCaptor.getValue().getHitsAverage(),
        0);
    ZonedDateTime alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));

    TrafficStatistic secondTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 5);
        }})
        .build();

    reset(this.eventBus);

    this.alertsManagerUnderTest.consumeTrafficStatistics(secondTrafficStatistics);

    verify(this.eventBus, times(0)).post(any());

  }

  @Test
  public void noRecoverAlertWithoutHighAlert() {
    TrafficStatistic firstTrafficStatistic = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 4);
          put("secondSection", 5);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(firstTrafficStatistic);

    verify(this.eventBus, times(0)).post(any());
  }

  @Test
  public void recoveredAlertAfterHighTraffic() {
    TrafficStatistic firstTrafficStatistic = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 5);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(firstTrafficStatistic);

    ArgumentCaptor<TrafficAlert> argumentCaptor = ArgumentCaptor.forClass(TrafficAlert.class);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals(firstTrafficStatistic.totalHits(),
        argumentCaptor.getValue().getHitsAverage(),
        0);
    ZonedDateTime alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));

    TrafficStatistic secondTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
        }})
        .build();

    reset(this.eventBus);

    this.alertsManagerUnderTest.consumeTrafficStatistics(secondTrafficStatistics);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.RECOVERED_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals((firstTrafficStatistic.totalHits() + secondTrafficStatistics.totalHits()) / 2,
        argumentCaptor.getValue().getHitsAverage(),
        0);
    alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));
  }

  @Test
  public void noRecoveredAlertIfAlreadyPublished() {
    TrafficStatistic firstTrafficStatistic = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 5);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(firstTrafficStatistic);

    ArgumentCaptor<TrafficAlert> argumentCaptor = ArgumentCaptor.forClass(TrafficAlert.class);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals(firstTrafficStatistic.totalHits(),
        argumentCaptor.getValue().getHitsAverage(),
        0);
    ZonedDateTime alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));

    TrafficStatistic secondTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
        }})
        .build();

    reset(this.eventBus);

    this.alertsManagerUnderTest.consumeTrafficStatistics(secondTrafficStatistics);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.RECOVERED_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals((firstTrafficStatistic.totalHits() + secondTrafficStatistics.totalHits()) / 2,
        argumentCaptor.getValue().getHitsAverage(),
        0);
    alertDateTime = argumentCaptor.getValue().getAlertDateTime();
    assertTrue(correctAlertDateTime(alertDateTime));

    TrafficStatistic thirdTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
        }})
        .build();

    reset(this.eventBus);

    this.alertsManagerUnderTest.consumeTrafficStatistics(thirdTrafficStatistics);
    verify(this.eventBus, times(0)).post(any());

  }

  @Test
  public void alertManagerGeneralTest() {

    TrafficStatistic firstTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 5);
          put("secondSection", 2);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(firstTrafficStatistics);

    verify(this.eventBus, times(0)).post(any());

    TrafficStatistic secondTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 13);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(secondTrafficStatistics);

    ArgumentCaptor<TrafficAlert> argumentCaptor = ArgumentCaptor.forClass(TrafficAlert.class);
    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.HIGH_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals((firstTrafficStatistics.totalHits() + secondTrafficStatistics.totalHits()) / 2,
        argumentCaptor.getValue().getHitsAverage(),
        0);

    reset(this.eventBus);

    TrafficStatistic thirdTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("secondSection", 1);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(thirdTrafficStatistics);

    verify(this.eventBus, times(1)).post(any());
    verify(this.eventBus).post(argumentCaptor.capture());

    assertEquals(TrafficAlert.AlertType.RECOVERED_TRAFFIC, argumentCaptor.getValue().getAlertType());
    assertEquals((firstTrafficStatistics.totalHits()
            + secondTrafficStatistics.totalHits()
            + thirdTrafficStatistics.totalHits()) / 3,
        argumentCaptor.getValue().getHitsAverage(),
        0);

    reset(this.eventBus);

    TrafficStatistic fourthTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 3);
        }})
        .build();

    this.alertsManagerUnderTest.consumeTrafficStatistics(fourthTrafficStatistics);
    verify(this.eventBus, times(0)).post(any());

    reset(this.eventBus);

    TrafficStatistic fifthTrafficStatistics = TrafficStatistic.builder()
        .sectionsHits(new HashMap<String, Integer>() {{
          put("firstSection", 20);
        }})
        .build();

    // Total hits = 13 + 1 + 3 + 20 = 37, average = 37 / 4 < 10 then no alerts
    this.alertsManagerUnderTest.consumeTrafficStatistics(fifthTrafficStatistics);
    verify(this.eventBus, times(0)).post(any());

  }

  private boolean correctAlertDateTime(ZonedDateTime alertDateTime) {
    return (alertDateTime.equals(this.testExecutionDateTime) || alertDateTime.isAfter(this.testExecutionDateTime))
        && (alertDateTime.equals(ZonedDateTime.now()) || alertDateTime.isBefore(ZonedDateTime.now()));
  }

  private void fillAlertBuffer(int alertBufferSize) {

  }

}
