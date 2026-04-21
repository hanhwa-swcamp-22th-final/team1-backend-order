package com.conk.order.command.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/*
 * Order 도메인 규칙 테스트.
 * 주문 aggregate 생성 규칙과 상태 전이 규칙을 검증한다.
 */
public class OrderTest {

  /* 정상 주문 생성 시 기본 상태가 RECEIVED(접수) 인지 확인한다. */
  @Test
  void createCreatesReceivedOrder() {
    Order order = createValidOrder();

    assertThat(order.getOrderId()).isEqualTo("ORD-20260327-001");
    assertThat(order.getOrderedAt()).isEqualTo(LocalDateTime.of(2026, 3, 27, 0, 0));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* 출고 완료 전후로 접수 상태 여부가 올바르게 바뀌는지 확인한다. */
  @Test
  void isReceivedReturnsTrueOnlyForReceivedStatus() {
    Order order = createValidOrder();

    assertThat(order.isReceived()).isTrue();

    order.markOutboundCompleted();

    assertThat(order.isReceived()).isFalse();
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

  /* RECEIVED·ALLOCATED 외 상태에서는 취소할 수 없는지 확인한다. */
  @Test
  void completedOrderCannotBeCanceled() {
    Order order = createValidOrder();
    order.markOutboundCompleted();

    assertThatThrownBy(order::cancelOrder)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Order cannot be canceled in current status.");
  }

  /* 주문 항목과 배송지가 포함된 aggregate 가 정상 생성되는지 확인한다. */
  @Test
  void createCreatesOrderWithItemsAndShippingAddress() {
    OrderItem item = createValidItem();
    ShippingAddress address = createValidAddress();

    Order order = Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(item),
        address,
        "홍길동",
        "010-1234-5678",
        null
    );

    assertThat(order.getItems()).hasSize(1);
    assertThat(order.getShippingAddress()).isEqualTo(address);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* 주문번호가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenOrderNoIsBlank() {
    assertThatThrownBy(() -> Order.create(
        " ",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(createValidItem()),
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order number is required.");
  }

  /* 주문 일시가 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenOrderedAtIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        null,
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(createValidItem()),
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order date is required.");
  }

  /* 셀러 식별자가 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenSellerIdIsBlank() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        " ",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(createValidItem()),
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Seller ID is required.");
  }

  /* 주문 항목이 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenItemsIsEmpty() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(),
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order items are required.");
  }

  /* 주문 항목이 null 이면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenItemsIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        null,
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Order items are required.");
  }

  /* 배송지가 없으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenShippingAddressIsNull() {
    assertThatThrownBy(() -> Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(createValidItem()),
        null,
        "홍길동",
        "010-1234-5678",
        null
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Shipping address is required.");
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createValidOrder() {
    return Order.create(
        "ORD-20260327-001",
        LocalDateTime.of(2026, 3, 27, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(createValidItem()),
        createValidAddress(),
        "홍길동",
        "010-1234-5678",
        null
    );
  }

  private OrderItem createValidItem() {
    return OrderItem.create("SKU-001", 2, null);
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
