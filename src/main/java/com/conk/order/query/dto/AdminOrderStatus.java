package com.conk.order.query.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;

/*
 * 관리자 주문 목록 화면 전용 grouped status.
 *
 * 내부 OrderStatus 를 masterAdmin 화면의 5단계 상태로 압축해 노출한다.
 */
public enum AdminOrderStatus {
  PENDING,
  CONFIRMED,
  PREPARING_ITEM,
  SHIPPED,
  CANCELLED;

  public static AdminOrderStatus from(OrderStatus rawStatus) {
    if (rawStatus == null) {
      return null;
    }

    return switch (rawStatus) {
      case RECEIVED -> PENDING;
      case ALLOCATED -> CONFIRMED;
      case OUTBOUND_INSTRUCTED, PICKING, PACKING, OUTBOUND_PENDING -> PREPARING_ITEM;
      case OUTBOUND_COMPLETED -> SHIPPED;
      case CANCELED -> CANCELLED;
    };
  }
}
