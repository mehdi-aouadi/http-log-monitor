package org.datadog.statitics;

import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TrafficStatistic {

  private final long totalTrafficSize;
  private final int totalHitsCount;
  private final int successRequestsCount;
  private final int clientErrorRequestCount;
  private final int serverErrorRequestCount;
  Map<String, Integer> sectionsHits;
  Map<String, Integer> methodsHits;

}
