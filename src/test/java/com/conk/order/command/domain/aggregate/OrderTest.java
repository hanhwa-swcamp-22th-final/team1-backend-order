package com.conk.order.command.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * Order 도메인 규칙 테스트.
 * 주문 aggregate 생성 규칙과 상태 전이 규칙을 검증한다.
 * */
public class OrderTest {

  /* 정상 주문 생성 시 기본 상태가 출고 대기인지 확인한다. */
  @Test
  void createCreatesPendingOutboundOrder() {
    Order order = createValidOrder();

    assertThat(order.getOrderNo()).isEqualTo("ORD-20260327-001");
    assertThat(order.getOrderDate()).isEqualTo(LocalDate.of(2026, 3, 27));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_OUTBOUND);
  }

  /* 출고 완료 전후로 출고 대기 여부가 올바르게 바뀌는지 확인한다. */
  @Test
  void isPendingOutboundReturnsTrueOnlyForPendingStatus() {
    Order order = createValidOrder();

    assertThat(order.isPendingOutbound()).isTrue();

    order.markOutboundCompleted();

    assertThat(order.isPendingOutbound()).isFalse();
  }


  /* 취소된 주문은 출고 완료 처리할 수 없는지 확인한다. */
  @Test
  void canceledOrderCannotBeMarkedAsOutboundCompleted() {
    Order order = createValidOrder();
    order.cancelOrder();

    assertThatThrownBy(order::markOutboundCompleted)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Canceled order cannot be completed.");
  }

  /* 주문번호가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenOrderNoIsBlank() {
    assertThatThrownBy(() -> Order.create(
        " ",
        LocalDate.of(2026, 3, 27),
        List.of(createValidItem()),
        createValidAddress()
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order number is required.");
  }

  /* 주문일자가 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenOrderDateIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        null,
        List.of(createValidItem()),
        createValidAddress()
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order date is required.");
  }

  /* 출고 완료된 주문은 취소할 수 없는지 확인한다. */
  @Test
  void completedOrderCannotBeCanceled() {
    Order order = createValidOrder();
    order.markOutboundCompleted();

    assertThatThrownBy(order::cancelOrder)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Completed order cannot be canceled.");
  }

  /* 주문 항목과 배송지가 포함된 aggregate가 정상 생성되는지 확인한다. */
  @Test
  void createCreatesOrderWithItemsAndShippingAddress() {
    OrderItem item = createValidItem();
    ShippingAddress address = createValidAddress();

    Order order = Order.create(
        "ORD-20260327-001",
        LocalDate.of(2026, 3, 27),
        List.of(item),
        address
    );
    assertThat(order.getItems()).hasSize(1);
    assertThat(order.getShippingAddress()).isEqualTo(address);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_OUTBOUND);
  }

  /* 주문 항목이 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenItemsIsEmpty() {
    ShippingAddress address = createValidAddress();

    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDate.of(2026, 3, 27),
        List.of(),
        address
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order items are required.");
  }

  /* 주문 항목이 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenItemsIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDate.of(2026, 3, 27),
        null,
        createValidAddress()
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order items are required.");
  }

  /* 배송지가 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenShippingAddressIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDate.of(2026, 3, 27),
        List.of(createValidItem()),
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Shipping address is required.");
  }

  private Order createValidOrder() {
    return Order.create(
        "ORD-20260327-001",
        LocalDate.of(2026, 3, 27),
        List.of(createValidItem()),
        createValidAddress()
    );
  }

  private OrderItem createValidItem() {
    return OrderItem.create("SKU-001", 2);
  }

  private ShippingAddress createValidAddress() {
    return ShippingAddress.create(
        "서울시 강남구 테헤란로 123",
        "101동 202호",
        "Seoul",
        "Seoul",
        "06236"
    );
  }
}
