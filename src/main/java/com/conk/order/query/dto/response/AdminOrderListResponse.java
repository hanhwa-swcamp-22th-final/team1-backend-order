package com.conk.order.query.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * 관리자 주문 목록 조회 응답 DTO.
 *
 * ApiResponse<AdminOrderListResponse> 형태로 래핑되어 반환된다.
 * { success: true, data: { orders: [...], totalCount: N, page: N, size: N } }
 *
 * @RequiredArgsConstructor: final 필드를 받는 생성자를 자동 생성한다.
 *   Service 에서 new AdminOrderListResponse(orders, totalCount, page, size) 로 생성한다.
 */
@Getter
@RequiredArgsConstructor
public class AdminOrderListResponse {

  /** 현재 페이지 주문 목록. */
  private final List<AdminOrderSummary> orders;

  /** 필터 조건에 해당하는 전체 주문 건수. 프론트 페이징 UI 계산에 사용. */
  private final int totalCount;

  /** 현재 페이지 번호 (0부터 시작). */
  private final int page;

  /** 페이지 크기. */
  private final int size;
}
