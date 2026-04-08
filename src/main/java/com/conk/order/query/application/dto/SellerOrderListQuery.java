package com.conk.order.query.application.dto;

import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/*
 * 셀러 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * 컨트롤러에서 @RequestParam 으로 받은 값들을 하나의 객체로 묶어
 * Mapper 에 전달한다. MyBatis XML 에서 #{sellerId}, #{status} 처럼
 * 이 객체의 필드명으로 바인딩된다.
 *
 * sellerId 는 필수, 나머지는 선택 필터다.
 */
@Getter
@Setter
public class SellerOrderListQuery {

  /** 셀러 식별자 (필수). WHERE seller_id = #{sellerId} 로 사용된다. */
  private String sellerId;

  /*
   * 주문 상태 필터 (선택).
   * null 이면 XML <if test="status != null"> 조건이 false 가 되어 필터 미적용.
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
   * 예: page=2, size=10 → offset=20 (21번째 행부터 조회)
   */
  public int getOffset() {
    return page * size;
  }
}
