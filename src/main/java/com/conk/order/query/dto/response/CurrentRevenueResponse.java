package com.conk.order.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentRevenueResponse {

  private final long totalRevenue;
  private final String trend;
  private final String trendLabel;
  private final String trendType;
}
