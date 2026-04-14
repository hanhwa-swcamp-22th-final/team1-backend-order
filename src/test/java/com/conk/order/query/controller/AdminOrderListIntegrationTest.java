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
 * ORD-005 관리자 주문 목록 조회 통합 테스트.
 *
 * 전체 스택(Controller → Service → MyBatis XML → H2 DB) 을 실제로 실행한다.
 * XML 의 동적 WHERE 절(<where>, <if>) 이 올바르게 동작하는지까지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminOrderListIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /*
   * 파라미터 없이 요청하면 전체 셀러의 주문이 모두 반환된다.
   * SELLER-001 1건 + SELLER-002 1건 = totalCount 2.
   */
  @Test
  void getAdminOrders_returnsAllOrdersWithoutFilter() throws Exception {
    orderRepository.saveAndFlush(createOrder("ORD-IT-001", "SELLER-001"));
    orderRepository.saveAndFlush(createOrder("ORD-IT-002", "SELLER-002"));

    mockMvc.perform(get("/orders/list")
            .header("X-Seller-Id", "MASTER-ADMIN-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(2))
        .andExpect(jsonPath("$.data.orders").isArray())
        .andExpect(jsonPath("$.data.orders[0].id").exists())
        .andExpect(jsonPath("$.data.orders[0].company").exists())
        .andExpect(jsonPath("$.data.orders[0].warehouse").exists())
        .andExpect(jsonPath("$.data.orders[0].channel").exists())
        .andExpect(jsonPath("$.data.orders[0].skuCount").exists())
        .andExpect(jsonPath("$.data.orders[0].qty").exists())
        .andExpect(jsonPath("$.data.orders[0].destState").exists())
        .andExpect(jsonPath("$.data.orders[0].status").value("PENDING"));
  }

  /*
   * sellerId 필터를 적용하면 해당 셀러의 주문만 반환된다.
   * SELLER-001 2건 + SELLER-002 1건 중 SELLER-001 만 조회 → totalCount 2.
   */
  @Test
  void getAdminOrders_filtersBySellerId() throws Exception {
    orderRepository.saveAndFlush(createOrder("ORD-IT-003", "SELLER-001"));
    orderRepository.saveAndFlush(createOrder("ORD-IT-004", "SELLER-001"));
    orderRepository.saveAndFlush(createOrder("ORD-IT-005", "SELLER-002"));

    mockMvc.perform(get("/orders/list")
            .header("X-Seller-Id", "MASTER-ADMIN-001")
            .param("sellerId", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCount").value(2))
        .andExpect(jsonPath("$.data.orders[0].company").value("SELLER-001"))
        .andExpect(jsonPath("$.data.orders[1].company").value("SELLER-001"));
  }

  /*
   * 주문이 없으면 빈 목록과 totalCount 0 을 반환한다.
   */
  @Test
  void getAdminOrders_returnsEmpty_whenNoOrders() throws Exception {
    mockMvc.perform(get("/orders/list")
            .header("X-Seller-Id", "MASTER-ADMIN-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCount").value(0))
        .andExpect(jsonPath("$.data.orders").isEmpty());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId,
        LocalDateTime.of(2026, 4, 5, 10, 0),
        sellerId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("서울시 강남구 테헤란로 123", null, "Seoul", null, "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
