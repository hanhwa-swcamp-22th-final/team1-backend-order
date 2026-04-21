package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternalOrderQueryServiceTest {

  @Mock
  private OrderRepository orderRepository;

  private InternalOrderQueryService service;

  @BeforeEach
  void setUp() {
    service = new InternalOrderQueryService(orderRepository);
  }

  @Test
  void getPendingOrders_queriesByTenantId() {
    Order order = createOrder("ORD-001", "SELLER-001", "TENANT-001");
    given(orderRepository.findAllByStatusAndTenantIdOrderByOrderedAtDesc(
        com.conk.order.command.domain.aggregate.OrderStatus.RECEIVED, "TENANT-001"))
        .willReturn(List.of(order));

    var result = service.getPendingOrders("TENANT-001");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getOrderId()).isEqualTo("ORD-001");
    verify(orderRepository).findAllByStatusAndTenantIdOrderByOrderedAtDesc(
        com.conk.order.command.domain.aggregate.OrderStatus.RECEIVED, "TENANT-001");
  }

  @Test
  void getOrder_queriesByOrderIdAndTenantId() {
    Order order = createOrder("ORD-002", "SELLER-001", "TENANT-001");
    given(orderRepository.findByOrderIdAndTenantId("ORD-002", "TENANT-001"))
        .willReturn(Optional.of(order));

    var result = service.getOrder("TENANT-001", "ORD-002");

    assertThat(result.getOrderId()).isEqualTo("ORD-002");
    verify(orderRepository).findByOrderIdAndTenantId("ORD-002", "TENANT-001");
  }

  private Order createOrder(String orderId, String sellerId, String tenantId) {
    return Order.create(
        orderId,
        LocalDateTime.of(2026, 4, 21, 10, 0),
        sellerId,
        tenantId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, "상품A")),
        ShippingAddress.create("서울시 강남구 테헤란로 123", null, "Seoul", "Seoul", "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
