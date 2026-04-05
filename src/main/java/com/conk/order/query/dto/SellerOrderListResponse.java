package com.conk.order.query.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * 셀러 주문 목록 조회 응답 DTO.
 *
 * 응답 JSON 구조:
 * {
 *   "success": true,
 *   "data": {
 *     "orders": [ { orderNo, orderedAt, status, ... }, ... ],
 *     "totalCount": 100,   ← 필터 조건에 맞는 전체 건수 (페이지 수 계산용)
 *     "page": 0,
 *     "size": 20
 *   }
 * }
 */
@Getter
@RequiredArgsConstructor
public class SellerOrderListResponse {

  /** 현재 페이지의 주문 목록. */
  private final List<SellerOrderSummary> orders;

  /*
   * 필터 조건에 맞는 전체 주문 수.
   * 프론트에서 "전체 N건 중 20개" 표시나 페이지 수 계산에 사용한다.
   * Mapper 에서 COUNT 쿼리를 별도로 실행해 채운다.
   */
  private final int totalCount;

  /** 현재 페이지 번호 (0부터 시작). */
  private final int page;

  /** 페이지 크기. */
  private final int size;
}
