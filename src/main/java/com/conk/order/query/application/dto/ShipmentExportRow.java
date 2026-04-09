package com.conk.order.query.application.dto;

import lombok.Getter;
import lombok.Setter;

/*
 * 송장 CSV 다운로드용 행 데이터.
 *
 * MyBatis 쿼리 결과를 매핑한다.
 * OUTBOUND_COMPLETED 상태의 비연동 채널(MANUAL, EXCEL) 주문 대상.
 */
@Getter
@Setter
public class ShipmentExportRow {

  /** 주문번호. */
  private String orderId;

  /** 송장번호. */
  private String invoiceNo;

  /** 주문 채널. */
  private String orderChannel;

  /** 수령인 이름. */
  private String receiverName;

  /** 수령인 연락처. */
  private String receiverPhoneNo;

  /** 기본주소. */
  private String address1;

  /** 상세주소. */
  private String address2;

  /** 도시. */
  private String city;

  /** 주/지역. */
  private String state;

  /** 우편번호. */
  private String zipCode;

  /** 국가. */
  private String country;

  /** 출고 완료 일시. */
  private String shippedAt;
}
