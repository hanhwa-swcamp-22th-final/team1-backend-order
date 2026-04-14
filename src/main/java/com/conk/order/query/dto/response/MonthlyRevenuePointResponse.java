package com.conk.order.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyRevenuePointResponse {

  private final String month;
  private final String label;
  private final long revenue;
}
