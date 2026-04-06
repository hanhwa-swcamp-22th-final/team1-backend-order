package com.conk.order.query.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ORD-006 주문 KPI 집계 응답 DTO.
 *
 * 기간 내 전체 주문 건수와 상태별 건수를 반환한다.
 * 상태 순서는 OrderStatus enum 흐름을 따른다.
 * masterAdmin 대시보드에서 주문 현황을 한눈에 파악하는 데 사용한다.
 *
 * ApiResponse<OrderKpiResponse> 형태로 래핑되어 반환된다.
 * { success: true, data: { totalCount: N, receivedCount: N, ... } }
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

  /** 피킹&패킹(PICKING_PACKING) 상태 주문 건수. */
  private final int pickingPackingCount;

  /** 출고 보류(OUTBOUND_PENDING) 상태 주문 건수. */
  private final int outboundPendingCount;

  /** 출고 완료(OUTBOUND_COMPLETED) 상태 주문 건수. */
  private final int outboundCompletedCount;

  /** 취소(CANCELED) 상태 주문 건수. */
  private final int canceledCount;
}