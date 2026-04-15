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
 * ORD-006 주문 KPI 집계 통합 테스트.
 *
 * 전체 스택(Controller → Service → MyBatis XML → H2 DB) 을 실제로 실행한다.
 * 상태별 COUNT 쿼리와 날짜 필터가 실제로 올바르게 동작하는지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderKpiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /*
   * RECEIVED 주문 2건을 삽입하면 totalCount=2, receivedCount=2 로 응답한다.
   * 나머지 상태는 0 이어야 한다.
   */
  @Test
  void getKpi_returnsCorrectCountsFromDb() throws Exception {
    orderRepository.save(createOrder("ORD-KPI-001", "SELLER-001"));
    orderRepository.save(createOrder("ORD-KPI-002", "SELLER-001"));

    mockMvc.perform(get("/orders/kpi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.todayTotal").value(2))
        .andExpect(jsonPath("$.data.pendingCount").value(2))
        .andExpect(jsonPath("$.data.pickingCount").value(0))
        .andExpect(jsonPath("$.data.shippedCount").value(0));
  }

  /* 주문이 없으면 모든 건수가 0 으로 응답한다. */
  @Test
  void getKpi_returnsAllZero_whenNoOrders() throws Exception {
    mockMvc.perform(get("/orders/kpi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.todayTotal").value(0))
        .andExpect(jsonPath("$.data.pendingCount").value(0));
  }

  /*
   * 날짜 필터를 적용하면 해당 기간의 주문만 집계한다.
   * 2026-04-05 주문 1건 삽입 후 startDate=2026-04-05 로 조회 → todayTotal=1.
   * 2026-04-04 이전 날짜는 포함되지 않는다.
   */
  @Test
  void getKpi_filtersCountByDate() throws Exception {
    /* 2026-04-05 주문 1건 */
    orderRepository.save(createOrderAtDate("ORD-KPI-003", LocalDateTime.of(2026, 4, 5, 10, 0)));
    /* 2026-04-01 주문 1건 — 필터 범위 밖 */
    orderRepository.save(createOrderAtDate("ORD-KPI-004", LocalDateTime.of(2026, 4, 1, 10, 0)));

    mockMvc.perform(get("/orders/kpi")
            .param("startDate", "2026-04-05")
            .param("endDate", "2026-04-05"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.todayTotal").value(1));
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, String sellerId) {
    return createOrderAtDate(orderId, LocalDateTime.of(2026, 4, 5, 10, 0));
  }

  private Order createOrderAtDate(String orderId, LocalDateTime orderedAt) {
    return Order.create(
        orderId,
        orderedAt,
        "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("서울시 강남구 테헤란로 123", null, "Seoul", null, "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
