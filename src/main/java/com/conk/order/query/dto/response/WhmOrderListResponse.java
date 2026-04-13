package com.conk.order.query.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ORD-007 창고 관리자 주문 목록 조회 응답 DTO.
 *
 * ApiResponse<WhmOrderListResponse> 형태로 래핑되어 반환된다.
 * { success: true, data: { orders: [...], totalCount: N, page: N, size: N } }
 */
@Getter
@RequiredArgsConstructor
public class WhmOrderListResponse {

  /** 현재 페이지 주문 목록. */
  private final List<WhmOrderSummary> orders;

  /** 필터 조건에 해당하는 전체 주문 건수. */
  private final int totalCount;

  /** 현재 페이지 번호 (0부터 시작). */
  private final int page;

  /** 페이지 크기. */
  private final int size;
}
