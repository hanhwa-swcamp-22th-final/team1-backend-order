package com.conk.order.command.domain.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * OrderItem 도메인 규칙 테스트.
 * 주문 항목은 SKU와 수량을 필수값으로 가지며,
 * 잘못된 값으로 생성되지 않도록 검증한다.
 */
public class OrderItemTest {

  /* 정상 SKU와 수량으로 주문 항목이 생성되는지 확인한다. */
  @Test
  void createCreatesOrderItemWithValidSkuAndQuantity() {
    OrderItem orderItem = OrderItem.create("SKU-001", 3, "루미에르 앰플 30ml");

    assertThat(orderItem.getSku()).isEqualTo("SKU-001");
    assertThat(orderItem.getQuantity()).isEqualTo(3);
    assertThat(orderItem.getProductNameSnapshot()).isEqualTo("루미에르 앰플 30ml");
  }

  /* 상품명 스냅샷 없이도 주문 항목이 생성되는지 확인한다. */
  @Test
  void createCreatesOrderItemWithoutProductNameSnapshot() {
    OrderItem orderItem = OrderItem.create("SKU-001", 3, null);

    assertThat(orderItem.getSku()).isEqualTo("SKU-001");
    assertThat(orderItem.getProductNameSnapshot()).isNull();
  }

  /* 피킹·패킹 수량의 초기값이 0 인지 확인한다. */
  @Test
  void createInitializesPickedAndPackedQuantityToZero() {
    OrderItem orderItem = OrderItem.create("SKU-001", 3, null);

    assertThat(orderItem.getPickedQuantity()).isEqualTo(0);
    assertThat(orderItem.getPackedQuantity()).isEqualTo(0);
  }

  /* SKU 가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenSkuIsBlank() {
    assertThatThrownBy(() -> OrderItem.create(" ", 3, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SKU is required.");
  }

  /* 수량이 1 미만이면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenQuantityIsLessThanOne() {
    assertThatThrownBy(() -> OrderItem.create("SKU-001", 0, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Quantity must be greater than zero.");
  }
}