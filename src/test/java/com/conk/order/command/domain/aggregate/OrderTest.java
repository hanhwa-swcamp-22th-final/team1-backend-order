package com.conk.order.command.domain.aggregate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;


public class OrderTest {

  @Test
  void createCreatesPendingOutboundOrder() {
    Order order = Order.create("ORD-20260327-001", LocalDate.of (2026, 3, 27));

    assertThat(order.getOrderNo()).isEqualTo("ORD-20260327-001");
    assertThat(order.getOrderDate()).isEqualTo(LocalDate.of(2026, 3, 27));
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_OUTBOUND);
  }

  @Test
  void isPendingOutboundReturnsTrueOnlyForPendingStatus() {
    Order order = Order.create("ORD-20260327-001", LocalDate.of(2026, 3, 27));

    assertThat(order.isPendingOutbound()).isTrue();

    order.markOutboundCompleted();

    assertThat(order.isPendingOutbound()).isFalse();
  }


  @Test
  void canceledOrderCannotBeMarkedAsOutboundCompleted() {
    Order order = Order.create("ORD-20260327-001", LocalDate.of(2026, 3, 27));
    order.cancel();

    assertThatThrownBy(order::markOutboundCompleted)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Canceled order cannot be completed.");
  }

}
