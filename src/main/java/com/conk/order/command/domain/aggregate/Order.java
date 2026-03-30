package com.conk.order.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class Order {

  @Id
  private String orderNo;

  private LocalDate orderDate;

  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  @Embedded
  private ShippingAddress shippingAddress;


  protected Order(){}
  /*
   * 외부에서 직접 생성자를 호출하지 못하게 막고,
   * create() 팩토리 메서드를 통해 생성 규칙을 강제한다.
   * */
  private Order(
      String orderNo,
      LocalDate orderDate,
      List<OrderItem> items,
      ShippingAddress shippingAddress,
      OrderStatus status
  ) {
    validateOrderNo(orderNo);
    validateOrderDate(orderDate);
    validateItems(items);
    validateShippingAddress(shippingAddress);
    this.orderNo = orderNo;
    this.orderDate = orderDate;
    this.shippingAddress = shippingAddress;
    this.status = status;

    for (OrderItem item : items) {
      addItem(item);
    }
  }

  /**
   *  - 주문 생성
   *    새 주문은 항상 출고 대기 상태로 시작한다는 규칙을 표현한다.
   *
   * @param orderNo 주문번호
   * @param orderDate 주문날짜
   * @param items 주문 항목 목록
   * @param shippingAddress 배송지
   *
   * @return Order
   */
  public static Order create(
      String orderNo,
      LocalDate orderDate,
      List<OrderItem> items,
      ShippingAddress shippingAddress
  ) {
    return new Order(
        orderNo,
        orderDate,
        items,
        shippingAddress,
        OrderStatus.PENDING_OUTBOUND);
  }

  /*
   * 출고 대기 여부를 반환한다.
   * 출고 처리나 통계 집계 시 대상 주문을 필터링하는 데 사용된다.
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
   * 단, 이미 출고 완료된 주문은 취소할 수 없다.
   * */
  public void cancelOrder() {
    if (status == OrderStatus.OUTBOUND_COMPLETED) {
      throw new IllegalStateException("Completed order cannot be canceled.");
    }
    this.status = OrderStatus.CANCELED;
  }

  private void addItem(OrderItem item) {
    item.assignOrder(this);
    this.items.add(item);
  }

  /**
   * - 주문번호
   * 주문 번호 필수 값 검증
   *
   * @param orderNo 주문번호
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
      throw new IllegalArgumentException("Order date is required.");
    }
  }

  private void validateItems(List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Order items are required.");
    }
  }

  private void validateShippingAddress(ShippingAddress shippingAddress) {
    if (shippingAddress == null) {
      throw new IllegalArgumentException("Shipping address is required.");
    }
  }
}
