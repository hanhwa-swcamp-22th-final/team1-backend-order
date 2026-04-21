package com.conk.order.command.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * 셀러 주문 취소 통합 테스트.
 *
 * Controller → Service → Repository → H2 DB 전체 스택 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CancelOrderIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /* RECEIVED 주문을 취소하면 DB 에 CANCELED 로 저장된다. */
  @Test
  void cancel_changesStatusInDb() throws Exception {
    Order order = createOrder("ORD-CANCEL-001", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(patch("/orders/seller/ORD-CANCEL-001/cancel")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Order updated = orderRepository.findById("ORD-CANCEL-001").orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(OrderStatus.CANCELED);
  }

  /* 타 셀러가 취소를 시도하면 404 를 반환한다. */
  @Test
  void cancel_returns404_whenDifferentSeller() throws Exception {
    Order order = createOrder("ORD-CANCEL-002", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(patch("/orders/seller/ORD-CANCEL-002/cancel")
            .header("X-Seller-Id", "SELLER-OTHER"))
        .andExpect(status().isNotFound());

    // 원래 상태 유지 확인
    Order unchanged = orderRepository.findById("ORD-CANCEL-002").orElseThrow();
    assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* PICKING 상태 주문은 취소할 수 없다. */
  @Test
  void cancel_returns409_whenNotCancelable() throws Exception {
    Order order = createOrder("ORD-CANCEL-003", "SELLER-001");
    order.changeStatus(OrderStatus.ALLOCATED);
    order.changeStatus(OrderStatus.OUTBOUND_INSTRUCTED);
    order.changeStatus(OrderStatus.PICKING);
    orderRepository.save(order);

    mockMvc.perform(patch("/orders/seller/ORD-CANCEL-003/cancel")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isConflict());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId, LocalDateTime.of(2026, 4, 9, 10, 0), sellerId, "TENANT-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", null
    );
  }
}
