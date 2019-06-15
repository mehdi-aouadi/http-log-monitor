package org.datadog.cli;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApplicationOptions {

  @Builder.Default
  private String filePath = "/tmp/access.log";
  @Builder.Default
  private int refreshFrequency = 10;
  @Builder.Default
  private int trafficThreshold = 100;
  @Builder.Default
  private int thresholdMonitoringDuration = 4;

}
