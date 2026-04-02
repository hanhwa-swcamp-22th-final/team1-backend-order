package com.conk.order.query.dto;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class OutboundStatsResponse {

  private final int pendingOutboundCount;
  private final String trend;
  private final String trendLabel;
  private final String trendType;
}

