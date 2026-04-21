package com.conk.order.command.domain.repository;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * OrderRepository 계층 테스트.
 * Order aggregate 가 DB에 올바르게 저장·조회되는지 검증한다.
 */
@DataJpaTest
public class OrderRepositoryTest {

  @Autowired
  private OrderRepository orderRepository;

  /* Order aggregate 가 저장 후 동일한 값으로 조회되는지 확인한다. */
  @Test
  void savePersistsOrderAggregate() {
    Order order = createOrder("ORD-001");

    Order saved = orderRepository.saveAndFlush(order);
    Order found = orderRepository.findById(saved.getOrderId()).orElseThrow();

    assertThat(found.getOrderId()).isEqualTo("ORD-001");
    assertThat(found.getItems()).hasSize(1);
    assertThat(found.getShippingAddress().getCity()).isEqualTo("Seoul");
  }

  /* RECEIVED 상태 주문 수를 정확히 집계하는지 확인한다. */
  @Test
  void countByStatusReturnsReceivedCount() {
    Order received1 = createOrder("ORD-001");
    Order received2 = createOrder("ORD-002");
    Order completed = createOrder("ORD-003");
    completed.markOutboundCompleted();

    orderRepository.saveAll(List.of(received1, received2, completed));
    orderRepository.flush();

    Long count = orderRepository.countByStatus(OrderStatus.RECEIVED);

    assertThat(count).isEqualTo(2L);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderNo) {
    return Order.create(
        orderNo,
        LocalDateTime.of(2026, 3, 30, 0, 0),
        "SELLER-001",
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 2, null)),
        ShippingAddress.create(
            "서울시 강남구 테헤란로 123",
            "101동 202호",
            "Seoul",
            "Seoul",
            "06236"
        ),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
