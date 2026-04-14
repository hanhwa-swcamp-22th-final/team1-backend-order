package com.conk.order.query.dto.request;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.PageableOrderListQuery;
import com.conk.order.query.dto.SellerOrderStatus;
import java.util.List;
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

  /** seller 화면 전용 grouped status 필터. */
  private SellerOrderStatus sellerStatus;

  /**
   * MyBatis WHERE IN (...) 바인딩용 raw status 목록.
   * sellerStatus 가 없으면 빈 리스트를 반환해 상태 필터를 생략한다.
   */
  public List<OrderStatus> getStatuses() {
    return sellerStatus == null ? List.of() : sellerStatus.toOrderStatuses();
  }
}
