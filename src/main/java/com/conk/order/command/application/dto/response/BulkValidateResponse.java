package com.conk.order.command.application.dto.response;

import java.util.List;
import lombok.Getter;

/*
 * 엑셀 사전 검증 응답 DTO.
 *
 * POST /orders/seller/bulk/validate 응답으로 사용한다.
 * 총 행 수, 유효 행 수, 오류 목록을 반환한다.
 */
@Getter
public class BulkValidateResponse {

  /** 총 데이터 행 수 (헤더 제외). */
  private final int totalRows;

  /** 유효한 행 수. */
  private final int validRows;

  /** 오류가 발견된 행 목록. */
  private final List<RowError> errors;

  public BulkValidateResponse(int totalRows, int validRows, List<RowError> errors) {
    this.totalRows = totalRows;
    this.validRows = validRows;
    this.errors = errors;
  }

  /** 개별 행 오류 정보. */
  @Getter
  public static class RowError {
    private final int row;
    private final String message;

    public RowError(int row, String message) {
      this.row = row;
      this.message = message;
    }
  }
}
