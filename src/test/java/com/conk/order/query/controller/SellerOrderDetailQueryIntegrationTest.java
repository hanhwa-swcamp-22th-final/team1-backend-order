package com.conk.order.query.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
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
 * 셀러 주문 상세 조회 통합 테스트.
 *
 * 검증 대상:
 *   - 본인 주문 조회 시 canCancel 포함 상세 반환
 *   - 타 셀러 주문 접근 시 404
 *   - 존재하지 않는 주문 조회 시 404
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SellerOrderDetailQueryIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /* RECEIVED 상태 주문을 조회하면 canCancel=true 를 반환한다. */
  @Test
  void getDetail_returnsDetailWithCanCancel() throws Exception {
    Order order = createOrder("ORD-SD-001", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(get("/orders/seller/ORD-SD-001")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.orderId").value("ORD-SD-001"))
        .andExpect(jsonPath("$.data.status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.canCancel").value(true))
        .andExpect(jsonPath("$.data.items[0].sku").value("SKU-001"));
  }

  /* 타 셀러가 조회하면 404 를 반환한다. */
  @Test
  void getDetail_returns404_whenDifferentSeller() throws Exception {
    Order order = createOrder("ORD-SD-002", "SELLER-001");
    orderRepository.save(order);

    mockMvc.perform(get("/orders/seller/ORD-SD-002")
            .header("X-User-Id", "SELLER-OTHER"))
        .andExpect(status().isNotFound());
  }

  /* 존재하지 않는 주문 조회 시 404. */
  @Test
  void getDetail_returns404_whenNotFound() throws Exception {
    mockMvc.perform(get("/orders/seller/NONEXISTENT")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isNotFound());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId, LocalDateTime.of(2026, 4, 9, 10, 0), sellerId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 2, "상품A")),
        ShippingAddress.create("123 Main St", "Apt 4", "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", "빠른배송"
    );
  }
}
