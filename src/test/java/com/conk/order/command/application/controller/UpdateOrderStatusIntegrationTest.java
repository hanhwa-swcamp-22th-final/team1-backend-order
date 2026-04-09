package com.conk.order.command.application.controller;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * 주문 상태 변경 통합 테스트.
 *
 * Controller → Service → Repository → H2 DB 전체 스택 검증.
 * 상태 전이 후 실제 DB 값이 변경되었는지 확인한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UpdateOrderStatusIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /* RECEIVED → ALLOCATED 정상 전이 후 DB 값이 변경된다. */
  @Test
  void updateStatus_changesStatusInDb() throws Exception {
    Order order = createOrder("ORD-STATUS-001");
    orderRepository.save(order);

    mockMvc.perform(patch("/orders/ORD-STATUS-001/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"ALLOCATED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    Order updated = orderRepository.findById("ORD-STATUS-001").orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(OrderStatus.ALLOCATED);
  }

  /* 연속 전이: RECEIVED → ALLOCATED → OUTBOUND_INSTRUCTED → PICKING. */
  @Test
  void updateStatus_supportsChainedTransitions() throws Exception {
    Order order = createOrder("ORD-STATUS-002");
    orderRepository.save(order);

    for (String status : List.of("ALLOCATED", "OUTBOUND_INSTRUCTED", "PICKING")) {
      mockMvc.perform(patch("/orders/ORD-STATUS-002/status")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"status\": \"" + status + "\"}"))
          .andExpect(status().isOk());
    }

    Order updated = orderRepository.findById("ORD-STATUS-002").orElseThrow();
    assertThat(updated.getStatus()).isEqualTo(OrderStatus.PICKING);
  }

  /* 잘못된 전이 시 409 Conflict 를 반환하고 상태는 그대로 유지된다. */
  @Test
  void updateStatus_returns409_andKeepsOriginalStatus() throws Exception {
    Order order = createOrder("ORD-STATUS-003");
    orderRepository.save(order);

    mockMvc.perform(patch("/orders/ORD-STATUS-003/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"PICKING\"}"))
        .andExpect(status().isConflict());

    Order unchanged = orderRepository.findById("ORD-STATUS-003").orElseThrow();
    assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.RECEIVED);
  }

  /* 존재하지 않는 주문 → 404. */
  @Test
  void updateStatus_returns404_whenOrderNotFound() throws Exception {
    mockMvc.perform(patch("/orders/NONEXISTENT/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\": \"ALLOCATED\"}"))
        .andExpect(status().isNotFound());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId) {
    return Order.create(
        orderId,
        LocalDateTime.of(2026, 4, 9, 10, 0),
        "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
