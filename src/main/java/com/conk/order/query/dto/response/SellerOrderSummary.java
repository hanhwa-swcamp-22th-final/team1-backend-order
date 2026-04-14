package com.conk.order.query.dto.response;

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
 * DB 컬럼명 order_id → orderId (AS 별칭 필요), ordered_at → orderedAt 처럼 자동 변환된다.
 *
 * @Setter 가 있어야 MyBatis 가 setter 를 통해 값을 채울 수 있다.
 */
@Getter
@Setter
public class SellerOrderSummary {

  /** 주문 식별자. SQL: o.order_id AS orderId */
  private String orderId;

  /** 주문 일시. SQL: o.ordered_at */
  private LocalDateTime orderedAt;

  /** 주문 상태. SQL: o.status */
  private OrderStatus status;

  /** 판매 채널. SQL: o.order_channel */
  private OrderChannel orderChannel;

  /** 화면 표시용 판매 채널명. orderChannel 기반 후처리 값 */
  private String channel;

  /** 수령인 이름. SQL: o.receiver_name */
  private String receiverName;

  /** 화면 표시용 수령인 이름. receiverName 기반 후처리 값 */
  private String recipient;

  /*
   * 주문 항목 수.
   * SQL: COUNT(i.id) AS itemCount
   * sales_order_item 테이블과 LEFT JOIN 후 집계한다.
   */
  private int itemCount;

  /** 화면 표시용 주문 품목 요약. itemCount 기반 후처리 값 */
  private String itemsSummary;

  /** 배송지 기본 주소. SQL: o.ship_to_address1 AS street1 */
  private String street1;

  /** 배송지 상세 주소. SQL: o.ship_to_address2 AS street2 */
  private String street2;

  /** 화면 표시용 주소. street1/street2 기반 후처리 값 */
  private String address;

  /** 배송지 주/지역. SQL: o.ship_to_state AS state */
  private String state;

  /** 배송지 우편번호. SQL: o.ship_to_zip_code AS zip */
  private String zip;

  /** 배송지 국가. SQL: o.ship_to_country AS country */
  private String country;

  /** 수령인 연락처. SQL: o.receiver_phone_no AS phone */
  private String phone;

  /** 운송장번호 응답값. SQL: o.tracking_code AS trackingNo */
  private String trackingNo;

  /** 취소 가능 여부. status 기반 후처리 값 */
  private boolean canCancel;
}
