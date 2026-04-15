package com.conk.order.query.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ORD-006 주문 KPI 집계 응답 DTO.
 *
 * 기간 내 전체 주문 건수와 상태별 건수를 반환한다.
 * 상태 순서는 OrderStatus enum 흐름을 따른다.
 */
@Getter
@RequiredArgsConstructor
public class OrderKpiResponse {

  /** 기간 내 전체 주문 건수. */
  private final int todayTotal;

  /** 대기(Pending) 상태 주문 건수 (RECEIVED + ALLOCATED + OUTBOUND_INSTRUCTED). */
  private final int pendingCount;

  /** 작업 중(Working) 상태 주문 건수 (PICKING + PACKING + OUTBOUND_PENDING). */
  private final int pickingCount;

  /** 출고 완료(Shipped) 상태 주문 건수 (OUTBOUND_COMPLETED). */
  private final int shippedCount;
}