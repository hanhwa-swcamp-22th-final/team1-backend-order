package com.conk.order.command.domain.aggregate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 * OrderItem 도메인 규칙 테스트
 * 주문 항목은 SKU와 수량을 최소 필수값으로 가지며,
 * 잘못된 값으로 생성되지 않도록 검증한다.
 * */
public class OrderItemTest {

  /* 정상 SKU와 수량으로 주문 항목이 생성되는지 확인한다. */
  @Test
  void createCreatesOrderItemWithValidSkuAndQuantity() {
    OrderItem orderItem = OrderItem.create("SKU-001", 3);

    assertThat(orderItem.getSku()).isEqualTo("SKU-001");
    assertThat(orderItem.getQuantity()).isEqualTo(3);

  }

  /* SKU가 비어 있으면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenSkuIsBlank() {
    assertThatThrownBy(() -> OrderItem.create(" ", 3))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SKU is required.");
  }

  /* 수량이 1 미만이면 생성할 수 없는지 확인한다. */
  @Test
  void createFailsWhenQuantityIsLessThanOne() {
    assertThatThrownBy(() -> OrderItem.create("SKU-001", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Quantity must be greater than zero.");
  }
}
