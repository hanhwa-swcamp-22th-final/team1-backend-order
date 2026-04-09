package com.conk.order.query.application.dto;

import com.conk.order.common.dto.PageableOrderListQuery;
import lombok.Getter;
import lombok.Setter;

/*
 * 셀러 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * sellerId 는 필수, 나머지(status, startDate, endDate, page, size)는
 * PageableOrderListQuery 에서 상속받는다.
 */
@Getter
@Setter
public class SellerOrderListQuery extends PageableOrderListQuery {

  /** 셀러 식별자 (필수). WHERE seller_id = #{sellerId} 로 사용된다. */
  private String sellerId;
}
