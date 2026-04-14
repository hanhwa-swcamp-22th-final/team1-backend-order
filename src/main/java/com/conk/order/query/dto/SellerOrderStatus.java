package com.conk.order.query.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;
import java.util.List;

/*
 * seller 주문 조회 API 전용 grouped status.
 *
 * 내부 OrderStatus 는 창고 운영 단계를 세분화하지만,
 * seller 화면에는 일부 상태를 묶은 값만 노출한다.
 */
public enum SellerOrderStatus {
  RECEIVED(List.of(OrderStatus.RECEIVED)),
  ALLOCATED(List.of(OrderStatus.ALLOCATED)),
  DISPATCHED(List.of(
      OrderStatus.OUTBOUND_INSTRUCTED,
      OrderStatus.PICKING,
      OrderStatus.PACKING
  )),
  WAITING(List.of(OrderStatus.OUTBOUND_PENDING)),
  COMPLETED(List.of(OrderStatus.OUTBOUND_COMPLETED)),
  CANCELED(List.of(OrderStatus.CANCELED));

  private final List<OrderStatus> rawStatuses;

  SellerOrderStatus(List<OrderStatus> rawStatuses) {
    this.rawStatuses = List.copyOf(rawStatuses);
  }

  public List<OrderStatus> toOrderStatuses() {
    return rawStatuses;
  }

  public static SellerOrderStatus from(OrderStatus rawStatus) {
    if (rawStatus == null) {
      return null;
    }

    return switch (rawStatus) {
      case RECEIVED -> RECEIVED;
      case ALLOCATED -> ALLOCATED;
      case OUTBOUND_INSTRUCTED, PICKING, PACKING -> DISPATCHED;
      case OUTBOUND_PENDING -> WAITING;
      case OUTBOUND_COMPLETED -> COMPLETED;
      case CANCELED -> CANCELED;
    };
  }
}
