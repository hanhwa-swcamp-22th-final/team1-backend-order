package com.conk.order.query.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * 주문 상태 트래킹 통합 테스트.
 *
 * 검증 대상:
 *   - 상태 변경 후 트래킹 조회 시 이력이 포함됨
 *   - 주문 취소 후 트래킹에 CANCELED 이력 포함
 *   - 이력이 없으면 빈 리스트 반환
 *   - 타 셀러 접근 시 404
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderTrackingQueryIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ObjectMapper objectMapper;

  /* 상태 변경 후 트래킹을 조회하면 이력이 포함된다. */
  @Test
  void getTracking_returnsHistoryAfterStatusChange() throws Exception {
    Order order = createOrder("ORD-TRACK-001", "SELLER-001");
    orderRepository.save(order);

    // RECEIVED → ALLOCATED 상태 변경
    mockMvc.perform(patch("/orders/ORD-TRACK-001/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("status", "ALLOCATED"))))
        .andExpect(status().isOk());

    // 트래킹 조회
    mockMvc.perform(get("/orders/seller/ORD-TRACK-001/tracking")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.orderId").value("ORD-TRACK-001"))
        .andExpect(jsonPath("$.data.currentStatus").value("ALLOCATED"))
        .andExpect(jsonPath("$.data.history.length()").value(1))
        .andExpect(jsonPath("$.data.history[0].fromStatus").value("RECEIVED"))
        .andExpect(jsonPath("$.data.history[0].toStatus").value("ALLOCATED"));
  }

  /* 취소 후 트래킹에 CANCELED 이력이 포함된다. */
  @Test
  void getTracking_includesCancelHistory() throws Exception {
    Order order = createOrder("ORD-TRACK-002", "SELLER-001");
    orderRepository.save(order);

    // 주문 취소
    mockMvc.perform(patch("/orders/seller/ORD-TRACK-002/cancel")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk());

    // 트래킹 조회
    mockMvc.perform(get("/orders/seller/ORD-TRACK-002/tracking")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentStatus").value("CANCELED"))
        .andExpect(jsonPath("$.data.history[0].fromStatus").value("RECEIVED"))
        .andExpect(jsonPath("$.data.history[0].toStatus").value("CANCELED"));
  }

  /* 이력이 없으면 빈 리스트를 반환한다. */
  @Test
  void getTracking_returnsEmptyHistory_whenNoChanges() throws Exception {
    Order order = createOrder("ORD-TRACK-003", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(get("/orders/seller/ORD-TRACK-003/tracking")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.currentStatus").value("RECEIVED"))
        .andExpect(jsonPath("$.data.history.length()").value(0));
  }

  /* 타 셀러가 접근하면 404. */
  @Test
  void getTracking_returns404_whenDifferentSeller() throws Exception {
    Order order = createOrder("ORD-TRACK-004", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(get("/orders/seller/ORD-TRACK-004/tracking")
            .header("X-Seller-Id", "SELLER-OTHER"))
        .andExpect(status().isNotFound());
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
