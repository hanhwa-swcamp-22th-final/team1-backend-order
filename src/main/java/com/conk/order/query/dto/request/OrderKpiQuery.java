package com.conk.order.query.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * ORD-006 주문 KPI 집계 쿼리 파라미터 DTO.
 *
 * 집계 기준 기간을 지정한다.
 * startDate/endDate 가 모두 null 이면 전체 기간 집계.
 */
@Getter
@Setter
public class OrderKpiQuery {

  /** 집계 시작일 (선택). null 이면 시작일 조건 미적용. */
  private LocalDate startDate;

  /** 집계 종료일 (선택). null 이면 종료일 조건 미적용. */
  private LocalDate endDate;

  public LocalDateTime getStartDateTime() {
    return startDate == null ? null : startDate.atStartOfDay();
  }

  public LocalDateTime getEndDateExclusive() {
    return endDate == null ? null : endDate.plusDays(1).atStartOfDay();
  }
}
