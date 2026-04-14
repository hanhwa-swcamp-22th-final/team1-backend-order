package com.conk.order.query.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SellerOrderCountRow {

  private String sellerId;
  private Integer totalOrders;
}
