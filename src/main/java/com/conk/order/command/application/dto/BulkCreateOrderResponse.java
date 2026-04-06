package com.conk.order.command.application.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ORD-003 엑셀 일괄 주문 등록 응답 DTO.
 *
 * successCount: 저장에 성공한 주문 수.
 * failedRows: 실패한 행 번호와 원인 목록 (없으면 빈 리스트).
 *
 * ApiResponse<BulkCreateOrderResponse> 형태로 래핑되어 반환된다.
 */
@Getter
@RequiredArgsConstructor
public class BulkCreateOrderResponse {

  /** 등록 성공 건수. */
  private final int successCount;

  /** 실패 행 목록. */
  private final List<FailedRow> failedRows;
}
