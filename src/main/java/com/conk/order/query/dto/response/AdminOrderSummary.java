package com.conk.order.query.dto.response;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.dto.AdminOrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * 관리자 주문 목록 조회 결과 항목 DTO.
 *
 * MyBatis XML 의 SELECT 결과가 이 객체에 매핑된다.
 * masterAdmin 화면이 바로 사용할 수 있는 키 형태로 응답을 구성한다.
 *
 * @Setter: MyBatis 가 ResultSet → DTO 매핑 시 setter 를 사용한다.
 */
@Getter
@Setter
public class AdminOrderSummary {

  /** 화면용 주문 식별자. SQL: o.order_id AS id */
  private String id;

  /** 주문 일시. sales_order.ordered_at */
  private LocalDateTime orderedAt;

  /** 내부 raw 주문 상태. SQL: o.status */
  @JsonIgnore
  private OrderStatus rawStatus;

  /** 관리자 화면에 노출하는 grouped 주문 상태. */
  private AdminOrderStatus status;

  /** 판매 채널. FE channelInfo 가 해석하는 raw 채널 키를 그대로 노출한다. */
  private String channel;

  /** 셀러사. 현재는 sellerId 를 화면용 company 값으로 사용한다. */
  private String company;

  /** 주문 배정 창고. 현재는 warehouseId 를 화면용 warehouse 값으로 사용한다. */
  private String warehouse;

  /** 주문 항목 종류 수. LEFT JOIN + COUNT(id) 로 계산. */
  private int skuCount;

  /** 총 주문 수량. LEFT JOIN + SUM(quantity) 로 계산. */
  private int qty;

  /** 배송지 주/지역. */
  private String destState;
}
