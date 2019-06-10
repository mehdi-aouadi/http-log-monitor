package org.datadog.alerts;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class TrafficAlert {

  @RequiredArgsConstructor
  public enum AlertType {
    HIGH_TRAFFIC("High traffic generated an alert - {0} hits/s\nTriggered at {1}"),
    RECOVERED_TRAFFIC("Recovered at {1} - {0} hits/s");

    /**
     * Message format used to construct the alert.
     */
    private final String message;
  }

  private AlertType alertType;
  private ZonedDateTime alertDateTime;
  private float hitsAverage;

}
