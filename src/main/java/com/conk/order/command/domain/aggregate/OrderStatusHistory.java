package com.conk.order.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

/*
 * 주문 상태 변경 히스토리 엔티티.
 *
 * 주문 상태가 변경될 때마다 이전 상태 → 새 상태를 기록한다.
 * 물리 테이블: order_status_history
 */
@Getter
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

  /** 히스토리 식별자. 자동 생성. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 소속 주문번호. */
  @Column(name = "order_id", nullable = false)
  private String orderId;

  /** 변경 전 상태. 최초 생성 시 null. */
  @Column(name = "from_status")
  @Enumerated(EnumType.STRING)
  private OrderStatus fromStatus;

  /** 변경 후 상태. */
  @Column(name = "to_status", nullable = false)
  @Enumerated(EnumType.STRING)
  private OrderStatus toStatus;

  /** 변경 일시. */
  @Column(name = "changed_at", nullable = false)
  private LocalDateTime changedAt;

  /** 변경자. */
  @Column(name = "changed_by")
  private String changedBy;

  protected OrderStatusHistory() {}

  /** 상태 변경 히스토리를 생성한다. */
  public static OrderStatusHistory create(
      String orderId, OrderStatus fromStatus, OrderStatus toStatus, String changedBy) {
    OrderStatusHistory history = new OrderStatusHistory();
    history.orderId = orderId;
    history.fromStatus = fromStatus;
    history.toStatus = toStatus;
    history.changedAt = LocalDateTime.now();
    history.changedBy = changedBy;
    return history;
  }
}
