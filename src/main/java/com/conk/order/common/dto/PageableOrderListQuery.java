package com.conk.order.common.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/*
 * 페이지네이션 + 필터링이 필요한 주문 목록 조회 쿼리의 공통 기반 클래스.
 *
 * status, startDate, endDate, page, size, getOffset() 을 공통으로 제공한다.
 * 하위 클래스는 각자 필요한 필드(sellerId, warehouseId 등)만 추가하면 된다.
 */
@Getter
@Setter
public abstract class PageableOrderListQuery {

  /** 주문 상태 필터 (선택). null 이면 상태 조건 미적용. */
  private OrderStatus status;

  /** 조회 시작일 (선택). null 이면 시작일 필터 미적용. */
  private LocalDate startDate;

  /** 조회 종료일 (선택). null 이면 종료일 필터 미적용. */
  private LocalDate endDate;

  /** 페이지 번호 (0부터 시작, 기본값 0). */
  private int page = 0;

  /** 페이지 크기 (기본값 20). */
  private int size = 20;

  /*
   * MyBatis SQL 의 OFFSET 계산용.
   * LIMIT #{size} OFFSET #{offset} 으로 사용된다.
   */
  public int getOffset() {
    return page * size;
  }
}
