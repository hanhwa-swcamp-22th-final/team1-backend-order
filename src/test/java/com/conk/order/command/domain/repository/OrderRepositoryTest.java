package com.conk.order.command.domain.repository;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class OrderRepositoryTest {

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void savePersistsOrderAggregate() {
    Order order = createOrder("ORD-001");

    Order saved = orderRepository.saveAndFlush(order);
    Order found = orderRepository.findById(saved.getOrderNo()).orElseThrow();

    assertThat(found.getOrderNo()).isEqualTo("ORD-001");
    assertThat(found.getItems()).hasSize(1);
    assertThat(found.getShippingAddress().getCity()).isEqualTo("Seoul");
  }

  @Test
  void countByStatusReturnsPendingOutboundCount() {
    Order pending1 = createOrder("ORD-001");
    Order pending2 = createOrder("ORD-002");
    Order completed = createOrder("ORD-003");
    completed.markOutboundCompleted();

    orderRepository.saveAll(List.of(pending1, pending2, completed));
    orderRepository.flush();

    Long count = orderRepository.countByStatus(OrderStatus.PENDING_OUTBOUND);

    assertThat(count).isEqualTo(2L);
  }

  private Order createOrder(String orderNo) {
    return Order.create(
        orderNo,
        LocalDate.of(2026, 3, 30),
        List.of(OrderItem.create("SKU-001", 2)),
        ShippingAddress.create(
            "서울시 강남구 테헤란로 123",
            "101동 202호",
            "Seoul",
            "Seoul",
            "06236"
        )
    );
  }
}
