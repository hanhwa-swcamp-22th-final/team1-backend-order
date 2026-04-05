package com.conk.order.query.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/*
 * ORD-007 창고 관리자(WHM) 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * warehouseId 는 필수값이다.
 * WHM 은 자신이 담당하는 창고의 주문만 조회할 수 있다.
 * 컨트롤러에서 @RequestParam 으로 받은 값들을 하나의 객체로 묶어 Mapper 에 전달한다.
 */
@Getter
@Setter
public class WhmOrderListQuery {

  /** 창고 식별자 (필수). WHERE warehouse_id = #{warehouseId} 로 사용된다. */
  private String warehouseId;

  /*
   * 주문 상태 필터 (선택).
   * null 이면 상태 조건 미적용.
   */
  private OrderStatus status;

  /** 조회 시작일 (선택). */
  private LocalDate startDate;

  /** 조회 종료일 (선택). */
  private LocalDate endDate;

  /** 페이지 번호 (0부터 시작, 기본값 0). */
  private int page = 0;

  /** 페이지 크기 (기본값 20). */
  private int size = 20;

  /* MyBatis SQL 의 OFFSET 계산용. */
  public int getOffset() {
    return page * size;
  }
}
