package com.conk.order.command.domain.aggregate;

import java.util.Map;
import java.util.Set;

/*
 * 주문 상태를 나타내는 열거형.
 *
 * 처리 흐름: RECEIVED → ALLOCATED → OUTBOUND_INSTRUCTED → PICKING → PACKING → OUTBOUND_COMPLETED
 *           OUTBOUND_PENDING 은 PACKING 이후 선택적으로 거칠 수 있다.
 *           어느 단계에서든 CANCELED 전환 가능 (단, OUTBOUND_COMPLETED 제외).
 *
 * VALID_TRANSITIONS 에 정의된 전이만 허용한다.
 */
public enum OrderStatus {

  /** 주문 접수 완료. */
  RECEIVED,

  /** 재고 할당 완료. */
  ALLOCATED,

  /** 출고 지시 완료. */
  OUTBOUND_INSTRUCTED,

  /** 피킹 진행 중. */
  PICKING,

  /** 패킹 진행 중. */
  PACKING,

  /** 출고 보류. */
  OUTBOUND_PENDING,

  /** 출고 완료. */
  OUTBOUND_COMPLETED,

  /** 주문 취소. */
  CANCELED;

  /*
   * 각 상태에서 전이 가능한 다음 상태 집합.
   * 여기에 없는 전이는 도메인 규칙 위반으로 거부한다.
   */
  private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
      RECEIVED,              Set.of(ALLOCATED, CANCELED),
      ALLOCATED,             Set.of(OUTBOUND_INSTRUCTED, CANCELED),
      OUTBOUND_INSTRUCTED,   Set.of(PICKING, CANCELED),
      PICKING,               Set.of(PACKING, CANCELED),
      PACKING,               Set.of(OUTBOUND_COMPLETED, OUTBOUND_PENDING, CANCELED),
      OUTBOUND_PENDING,      Set.of(OUTBOUND_COMPLETED, CANCELED),
      OUTBOUND_COMPLETED,    Set.of(),
      CANCELED,              Set.of()
  );

  /* 현재 상태에서 target 상태로 전이 가능한지 확인한다. */
  public boolean canTransitionTo(OrderStatus target) {
    Set<OrderStatus> allowed = VALID_TRANSITIONS.get(this);
    return allowed != null && allowed.contains(target);
  }
}