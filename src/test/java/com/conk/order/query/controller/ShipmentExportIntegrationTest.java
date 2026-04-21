package com.conk.order.query.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/*
 * 송장 CSV 다운로드 통합 테스트.
 *
 * 검증 대상:
 *   - OUTBOUND_COMPLETED + MANUAL/EXCEL 채널 주문만 CSV 에 포함
 *   - CSV 헤더 + 데이터 행 형식 검증
 *   - 데이터 없으면 헤더만 반환
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShipmentExportIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /* 출고 완료된 MANUAL 주문이 CSV 에 포함된다. */
  @Test
  void exportCsv_includesOutboundCompletedManualOrders() throws Exception {
    Order order = createOrder("ORD-CSV-001", OrderChannel.MANUAL);
    // 상태를 OUTBOUND_COMPLETED 까지 진행
    order.changeStatus(OrderStatus.ALLOCATED);
    order.changeStatus(OrderStatus.OUTBOUND_INSTRUCTED);
    order.changeStatus(OrderStatus.PICKING);
    order.changeStatus(OrderStatus.PACKING);
    order.changeStatus(OrderStatus.OUTBOUND_COMPLETED);
    orderRepository.save(order);

    MvcResult result = mockMvc.perform(get("/orders/shipments/export"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=shipment_export.csv"))
        .andReturn();

    String csv = result.getResponse().getContentAsString();
    assertThat(csv).contains("주문번호,송장번호");
    assertThat(csv).contains("ORD-CSV-001");
  }

  /* 데이터가 없으면 헤더만 반환한다. */
  @Test
  void exportCsv_returnsHeaderOnly_whenNoData() throws Exception {
    MvcResult result = mockMvc.perform(get("/orders/shipments/export"))
        .andExpect(status().isOk())
        .andReturn();

    String csv = result.getResponse().getContentAsString();
    String[] lines = csv.trim().split("\n");
    assertThat(lines).hasSize(1);
    assertThat(lines[0]).contains("주문번호");
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private Order createOrder(String orderId, OrderChannel channel) {
    return Order.create(
        orderId, LocalDateTime.of(2026, 4, 9, 10, 0), "SELLER-001", "TENANT-001",
        channel,
        List.of(OrderItem.create("SKU-001", 1, "상품A")),
        ShippingAddress.create("123 Main St", null, "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", null
    );
  }
}
