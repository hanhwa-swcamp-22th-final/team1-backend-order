package com.conk.order.query.application.controller;

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
 * ORD-007 창고 관리자 주문 목록 조회 통합 테스트.
 *
 * 전체 스택(Controller → Service → MyBatis XML → MariaDB) 을 실제로 실행한다.
 * warehouseId 기준 창고별 조회 제한과 선택 필터(status/날짜)가 올바르게 동작하는지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WhmOrderListIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /*
   * warehouseId 로 조회하면 해당 창고의 주문만 반환된다.
   * WH-001 에 2건, WH-002 에 1건 삽입 → WH-001 조회 시 totalCount=2.
   */
  @Test
  void getWhmOrders_returnsOnlyOrdersForGivenWarehouse() throws Exception {
    orderRepository.save(createOrder("ORD-WHM-001", "WH-001"));
    orderRepository.save(createOrder("ORD-WHM-002", "WH-001"));
    orderRepository.save(createOrder("ORD-WHM-003", "WH-002"));

    mockMvc.perform(get("/orders/whm")
            .param("warehouseId", "WH-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(2))
        .andExpect(jsonPath("$.data.orders").isArray());
  }

  /*
   * warehouseId 에 해당하는 주문이 없으면 빈 목록과 totalCount 0 을 반환한다.
   */
  @Test
  void getWhmOrders_returnsEmpty_whenNoOrdersForWarehouse() throws Exception {
    mockMvc.perform(get("/orders/whm")
            .param("warehouseId", "WH-999"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCount").value(0))
        .andExpect(jsonPath("$.data.orders").isEmpty());
  }

  /*
   * 날짜 필터를 적용하면 해당 기간의 주문만 집계한다.
   * WH-001 에 2026-04-05 주문 1건, 2026-04-01 주문 1건 삽입.
   * startDate=2026-04-05 로 조회 → totalCount=1.
   */
  @Test
  void getWhmOrders_filtersOrdersByDate() throws Exception {
    orderRepository.save(createOrderAtDate("ORD-WHM-004", "WH-001",
        LocalDateTime.of(2026, 4, 5, 10, 0)));
    orderRepository.save(createOrderAtDate("ORD-WHM-005", "WH-001",
        LocalDateTime.of(2026, 4, 1, 10, 0)));

    mockMvc.perform(get("/orders/whm")
            .param("warehouseId", "WH-001")
            .param("startDate", "2026-04-05")
            .param("endDate", "2026-04-05"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCount").value(1));
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderNo, String warehouseId) {
    return createOrderAtDate(orderNo, warehouseId, LocalDateTime.of(2026, 4, 5, 10, 0));
  }

  private Order createOrderAtDate(String orderNo, String warehouseId, LocalDateTime orderedAt) {
    Order order = Order.create(
        orderNo,
        orderedAt,
        "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("서울시 강남구 테헤란로 123", null, "Seoul", null, "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
    order.assignWarehouse(warehouseId);
    return order;
  }
}
