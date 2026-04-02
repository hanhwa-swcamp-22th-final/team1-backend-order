package com.conk.order.command.domain.aggregate;

/*
 * 주문 상태를 나타내는 열거형.
 *
 * 처리 흐름: RECEIVED → ALLOCATED → OUTBOUND_INSTRUCTED → PICKING → PACKING
 *           → OUTBOUND_PENDING(선택) → OUTBOUND_COMPLETED
 * 어느 단계에서든 CANCELED 전환 가능 (단, OUTBOUND_COMPLETED 제외).
 */
public enum OrderStatus {

  /** 주문 접수 완료. 출고 대기 집계 기준 상태. */
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
  CANCELED
}