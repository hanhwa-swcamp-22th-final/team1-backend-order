package com.conk.order.query.dto.response;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.OrderStatusHistory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

/*
 * 주문 상태 트래킹 응답 DTO.
 *
 * GET /orders/seller/{orderId}/tracking 응답으로 사용한다.
 * 주문의 상태 변경 이력을 시간순으로 반환한다.
 */
@Getter
public class OrderTrackingResponse {

  /** 주문번호. */
  private final String orderId;

  /** 현재 상태. */
  private final OrderStatus currentStatus;

  /** 상태 변경 이력 목록 (시간순). */
  private final List<StatusChange> history;

  public OrderTrackingResponse(String orderId, OrderStatus currentStatus,
      List<StatusChange> history) {
    this.orderId = orderId;
    this.currentStatus = currentStatus;
    this.history = history;
  }

  /** 개별 상태 변경 이력. */
  @Getter
  public static class StatusChange {
    private final OrderStatus fromStatus;
    private final OrderStatus toStatus;
    private final LocalDateTime changedAt;
    private final String changedBy;

    private StatusChange(OrderStatusHistory h) {
      this.fromStatus = h.getFromStatus();
      this.toStatus = h.getToStatus();
      this.changedAt = h.getChangedAt();
      this.changedBy = h.getChangedBy();
    }

    public static StatusChange from(OrderStatusHistory h) {
      return new StatusChange(h);
    }
  }
}
