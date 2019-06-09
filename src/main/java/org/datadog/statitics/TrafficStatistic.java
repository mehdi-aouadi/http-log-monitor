package org.datadog.statitics;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class TrafficStatistic {
  long totalTrafficSize;
  Map<String, Integer> sectionsHits;
}
