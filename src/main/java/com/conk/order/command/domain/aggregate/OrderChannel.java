package com.conk.order.command.domain.aggregate;

/*
 * 주문이 유입된 판매 채널을 나타내는 열거형.
 *
 * MANUAL / EXCEL: 셀러가 직접 입력 또는 업로드한 주문.
 * SHOPIFY: 외부 채널 연동으로 유입된 주문.
 *
 * 추가 채널 연동 시 이 열거형에 값을 추가하고 DB ENUM 컬럼도 함께 변경한다.
 */
public enum OrderChannel {

  /**
   * 수동 직접 입력.
   */
  MANUAL,

  /**
   * 엑셀 업로드.
   */
  EXCEL,

  /**
   * 쇼피파이 채널.
   */
  SHOPIFY,

}