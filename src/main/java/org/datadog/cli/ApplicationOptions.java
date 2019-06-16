package org.datadog.cli;

import lombok.Builder;
import lombok.Value;

/**
 * The {@link org.datadog.HttpLogMonitoringApplication} options.
 */
@Value
@Builder
public class ApplicationOptions {

  /**
   * The log file path.
   */
  @Builder.Default
  private String filePath = "/tmp/access.log";

  /**
   * The period in seconds after which the traffic statistics are computed.
   */
  @Builder.Default
  private int refreshFrequency = 10;

  /**
   * The traffic hits count threshold to trigger a traffic alert.
   */
  @Builder.Default
  private int trafficThreshold = 100000;

  /**
   *
   */
  @Builder.Default
  private int thresholdAlertCycles = 6;

}
