package com.conk.order.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyRevenueAggregationRow {

  private String month;
  private String sellerId;
  private String sku;
  private Integer quantity;
}
