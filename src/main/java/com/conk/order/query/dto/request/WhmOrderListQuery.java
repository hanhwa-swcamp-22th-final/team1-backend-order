package com.conk.order.query.dto.request;

import com.conk.order.common.dto.PageableOrderListQuery;
import lombok.Getter;
import lombok.Setter;

/*
 * ORD-007 창고 관리자(WHM) 주문 목록 조회 쿼리 파라미터 DTO.
 *
 * warehouseId 는 필수값이다.
 * WHM 은 자신이 담당하는 창고의 주문만 조회할 수 있다.
 */
@Getter
@Setter
public class WhmOrderListQuery extends PageableOrderListQuery {

  /** 창고 식별자 (필수). WHERE warehouse_id = #{warehouseId} 로 사용된다. */
  private String warehouseId;
}
