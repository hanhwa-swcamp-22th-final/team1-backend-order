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
  private final int totalCount;

  /** 접수(RECEIVED) 상태 주문 건수. */
  private final int receivedCount;

  /** 재고 할당(ALLOCATED) 상태 주문 건수. */
  private final int allocatedCount;

  /** 출고 지시(OUTBOUND_INSTRUCTED) 상태 주문 건수. */
  private final int outboundInstructedCount;

  /** 피킹(PICKING) 상태 주문 건수. */
  private final int pickingCount;

  /** 패킹(PACKING) 상태 주문 건수. */
  private final int packingCount;

  /** 출고 보류(OUTBOUND_PENDING) 상태 주문 건수. */
  private final int outboundPendingCount;

  /** 출고 완료(OUTBOUND_COMPLETED) 상태 주문 건수. */
  private final int outboundCompletedCount;

  /** 취소(CANCELED) 상태 주문 건수. */
  private final int canceledCount;
}