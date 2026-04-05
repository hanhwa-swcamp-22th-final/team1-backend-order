package com.conk.order.query.dto;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * 셀러 주문 목록의 개별 주문 요약 DTO.
 *
 * MyBatis 가 SQL 결과(ResultSet) 를 이 객체로 자동 매핑한다.
 * application.yml 의 map-underscore-to-camel-case: true 설정 덕분에
 * DB 컬럼명 order_id → orderNo (AS 별칭 필요), ordered_at → orderedAt 처럼 자동 변환된다.
 *
 * @Setter 가 있어야 MyBatis 가 setter 를 통해 값을 채울 수 있다.
 */
@Getter
@Setter
public class SellerOrderSummary {

  /** 주문번호. SQL: o.order_id AS orderNo */
  private String orderNo;

  /** 주문 일시. SQL: o.ordered_at */
  private LocalDateTime orderedAt;

  /** 주문 상태. SQL: o.status */
  private OrderStatus status;

  /** 판매 채널. SQL: o.order_channel */
  private OrderChannel orderChannel;

  /** 수령인 이름. SQL: o.receiver_name */
  private String receiverName;

  /*
   * 주문 항목 수.
   * SQL: COUNT(i.id) AS itemCount
   * sales_order_item 테이블과 LEFT JOIN 후 집계한다.
   */
  private int itemCount;
}
