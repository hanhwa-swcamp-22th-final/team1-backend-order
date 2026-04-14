package com.conk.order.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerRevenueResponse {

  private final String sellerId;
  private final long monthRevenue;
  private final int totalOrders;
  private final long avgOrderValue;
}
