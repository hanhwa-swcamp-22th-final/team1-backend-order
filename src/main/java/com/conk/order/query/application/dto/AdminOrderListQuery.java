package com.conk.order.query.application.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/*
 * 관리자 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * ORD-004(SellerOrderListQuery)와 달리 sellerId 가 필수가 아니다.
 * masterAdmin 은 모든 셀러의 주문을 조회할 수 있으므로 sellerId 는 선택 필터다.
 *
 * 컨트롤러에서 @RequestParam 으로 받은 값들을 하나의 객체로 묶어
 * MyBatis Mapper 에 전달한다.
 */
@Getter
@Setter
public class AdminOrderListQuery {

  /*
   * 셀러 필터 (선택).
   * null 이면 XML <if test="sellerId != null"> 조건이 false 가 되어 전체 셀러 조회.
   */
  private String sellerId;

  /*
   * 주문 상태 필터 (선택).
   * null 이면 상태 조건 미적용.
   */
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