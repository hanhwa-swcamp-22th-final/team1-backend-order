package com.conk.order.query.application.dto;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/*
 * ORD-007 창고 관리자 주문 목록 조회 결과 항목 DTO.
 *
 * WHM 화면에서 필요한 필드를 담는다.
 * sellerId 는 포함하지 않는다 — WHM 은 셀러 구분 없이 창고 단위로 주문을 처리한다.
 *
 * @Setter: MyBatis 가 ResultSet → DTO 매핑 시 setter 를 사용한다.
 */
@Getter
@Setter
public class WhmOrderSummary {

  /** 주문번호. */
  private String orderNo;

  /** 주문 일시. */
  private LocalDateTime orderedAt;

  /** 주문 상태. */
  private OrderStatus status;

  /** 판매 채널. */
  private OrderChannel orderChannel;

  /** 수령인 이름. */
  private String receiverName;

  /** 주문 항목 수. */
  private int itemCount;

  /** 배송지 기본 주소. */
  private String street1;

  /** 배송지 상세 주소. */
  private String street2;

  /** 배송지 주/지역. */
  private String state;

  /** 배송지 우편번호. */
  private String zip;

  /** 배송지 국가. */
  private String country;

  /** 수령인 연락처. */
  private String phone;
}
