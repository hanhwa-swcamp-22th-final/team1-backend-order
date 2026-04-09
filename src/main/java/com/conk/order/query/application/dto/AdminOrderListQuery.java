package com.conk.order.query.application.dto;

import com.conk.order.common.dto.PageableOrderListQuery;
import lombok.Getter;
import lombok.Setter;

/*
 * 관리자 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * SellerOrderListQuery 와 달리 sellerId 가 선택 필터다.
 * masterAdmin 은 모든 셀러의 주문을 조회할 수 있다.
 */
@Getter
@Setter
public class AdminOrderListQuery extends PageableOrderListQuery {

  /*
   * 셀러 필터 (선택).
   * null 이면 XML <if test="sellerId != null"> 조건이 false 가 되어 전체 셀러 조회.
   */
  private String sellerId;
}