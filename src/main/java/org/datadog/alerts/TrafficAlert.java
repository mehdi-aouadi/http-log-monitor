package org.datadog.alerts;

import java.text.MessageFormat;
import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@Value
public class TrafficAlert {

  @Getter
  @RequiredArgsConstructor
  public enum AlertType {
    HIGH_TRAFFIC("High traffic detected - hits on average = {0} triggered at  {1}"),
    RECOVERED_TRAFFIC("High traffic recovered - hits on average = {0} triggered at {1}");

    /**
     * Message format used to construct the alert.
     */
    private final String message;
  }

  private final AlertType alertType;
  private final ZonedDateTime alertDateTime;
  private final float hitsAverage;
  private final String message;

  /**
   * Creates a new immutable {@link TrafficAlert}.
   * @param alertType The type of the alert. Can be {@link AlertType#HIGH_TRAFFIC} or
   * {@link AlertType#RECOVERED_TRAFFIC}.
   * @param alertDateTime The alert triggering date time.
   * @param hitsAverage The number of hits on average that triggered the alert.
   */
  @Builder
  public TrafficAlert(AlertType alertType, ZonedDateTime alertDateTime, float hitsAverage) {
    this.alertType = alertType;
    this.alertDateTime = alertDateTime;
    this.hitsAverage = hitsAverage;
    if (this.alertType != null && this.alertDateTime != null) {
      this.message = MessageFormat.format(
          this.alertType.getMessage(),
          this.hitsAverage,
          this.alertDateTime.toLocalTime().withNano(0)
      );
    } else {
      this.message = null;
    }
  }

}
