package com.conk.order.command.domain.aggregate;

import java.time.LocalDate;

public class Order {

  /*
   * - 주문번호
   *   주문을 식별하는 핵심 값이므로 null 또는 blank 를 허용하지 않는다.
   * */
  private final String orderNo;

  /*
   * - 주문일자
   *   통계나 조회 기준이 되는 값이므로 null을 허용하지 않는다.
   * */
  private final LocalDate orderDate;

  /*
   * - 주문 상태
   *   생성 이후 cancel(), markOutboundCompleted() 같은 도메인 행위에 의해 바뀐다.
   * */
  private OrderStatus status;

  /*
   * 외부에서 직접 생성자를 호출하지 못하게 막고,
   * create() 팩토리 메서드를 통해 생성 규칙을 강제한다.
   * */
  private Order(String orderNo, LocalDate orderDate, OrderStatus status) {
    validateOrderNo(orderNo);
    validateOrderDate(orderDate);
    this.orderNo = orderNo;
    this.orderDate = orderDate;
    this.status = status;
  }

  /**
   * - 주문 생성
   * 새 주문은 기본적으로 PENDING_OUTBOUND 상태로 시작한다.
   *
   * @param orderNo   주문번호
   * @param orderDate 주문일자
   * @return
   */
  public static Order create(String orderNo, LocalDate orderDate) {
    return new Order(orderNo, orderDate, OrderStatus.PENDING_OUTBOUND);
  }

  /*
   * - 주문 생성 팩토리 메서드
   *   새 주문은 하상 출고 대기 상태로 시작한다는 규칙을 표현한다.
   * */
  public boolean isPendingOutbound() {
    return status == OrderStatus.PENDING_OUTBOUND;
  }

  /*
   * 주문을 출고 완료 상태로 변경한다.
   *
   * 단, 이미 취소된 주문은 출고 완료 처리할 수 없다.
   * 이 규칙을 어기면 예외를 발생시킨다.
   * */
  public void markOutboundCompleted() {
    if (status == OrderStatus.CANCELED) {
      throw new IllegalStateException("Canceled order cannot be completed.");
    }
    this.status = OrderStatus.OUTBOUND_COMPLETED;
  }

  /*
   * 주문을 취소 상태로 변경한다.
   *
   * 현재는 단순히 상태만 바꾸지만,
   * 이후 정책에 따라 "이미 출고 완료된 주문은 취소 불가" 같은 규칙을 추가할 수 있다.
   * */
  public void cancel() {
    this.status = OrderStatus.CANCELED;
  }

  /*
   * 테스트 검증용 getter.
   * 현재 단계에서는 도메인 상태를확인하기 위해 최소한으로 열어둔다.
   * */
  public String getOrderNo() {
    return orderNo;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public OrderStatus getStatus() {
    return status;
  }

  /**
   * - 주문번호
   * 주문 번호 필수 값 검증
   *
   * @param orderNo
   */
  private void validateOrderNo(String orderNo) {
    if (orderNo == null || orderNo.isBlank()) {
      throw new IllegalArgumentException("Order number is required.");
    }
  }

  /**
   * - 주문일자
   * 도메인 규칙상 주문일자는 필수값
   *
   * @param orderDate 주문일자
   */
  private void validateOrderDate(LocalDate orderDate) {
    if (orderDate == null) {
      throw new IllegalArgumentException("Order date is required");
    }
  }
}