package com.conk.order.query.dto;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * 관리자 주문 목록 조회 결과 항목 DTO.
 *
 * MyBatis XML 의 SELECT 결과가 이 객체에 매핑된다.
 * SellerOrderSummary 와 달리 sellerId 를 포함한다.
 * masterAdmin 은 여러 셀러의 주문을 한 목록에서 보므로 어느 셀러의 주문인지 표시해야 한다.
 *
 * @Setter: MyBatis 가 ResultSet → DTO 매핑 시 setter 를 사용한다.
 */
@Getter
@Setter
public class AdminOrderSummary {

  /** 주문번호. sales_order.order_id */
  private String orderNo;

  /** 주문 일시. sales_order.ordered_at */
  private LocalDateTime orderedAt;

  /** 주문 상태. sales_order.status */
  private OrderStatus status;

  /** 판매 채널. sales_order.order_channel */
  private OrderChannel orderChannel;

  /** 셀러 식별자. masterAdmin 화면에서 어느 셀러의 주문인지 표시하는 데 사용. */
  private String sellerId;

  /** 수령인 이름. sales_order.receiver_name */
  private String receiverName;

  /** 주문 항목 수. LEFT JOIN + COUNT(id) 로 계산. */
  private int itemCount;
}
