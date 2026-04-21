package com.conk.order.query.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * ORD-004 셀러 주문 목록 조회 통합 테스트.
 *
 * Controller → Service → MyBatis XML → DB 전체 흐름에서
 * 화면용 추가 필드와 기존 fallback 필드가 함께 반환되는지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SellerOrderListQueryIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  @Test
  void getSellerOrders_returnsDisplayFieldsAndFallbackFields() throws Exception {
    Order order = createOrder("ORD-SL-001", "SELLER-001");
    setField(order, "trackingCode", "TRK-001");
    orderRepository.save(order);

    mockMvc.perform(get("/orders/seller/list")
            .header("X-Seller-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.orders[0].orderId").value("ORD-SL-001"))
        .andExpect(jsonPath("$.data.orders[0].orderChannel").value("MANUAL"))
        .andExpect(jsonPath("$.data.orders[0].channel").value("MANUAL"))
        .andExpect(jsonPath("$.data.orders[0].receiverName").value("홍길동"))
        .andExpect(jsonPath("$.data.orders[0].recipient").value("홍길동"))
        .andExpect(jsonPath("$.data.orders[0].street1").value("서울시 강남구 테헤란로 123"))
        .andExpect(jsonPath("$.data.orders[0].street2").value("101동 202호"))
        .andExpect(jsonPath("$.data.orders[0].address").value("서울시 강남구 테헤란로 123 101동 202호"))
        .andExpect(jsonPath("$.data.orders[0].itemCount").value(2))
        .andExpect(jsonPath("$.data.orders[0].itemsSummary").value("상품 2건"))
        .andExpect(jsonPath("$.data.orders[0].trackingNo").value("TRK-001"))
        .andExpect(jsonPath("$.data.orders[0].canCancel").value(true))
        .andExpect(jsonPath("$.data.orders[0].status").value("RECEIVED"));
  }

  @Test
  void getSellerOrders_filtersAndReturnsGroupedDispatchStatus() throws Exception {
    Order dispatchedOrder = createOrder("ORD-SL-002", "SELLER-001");
    setField(dispatchedOrder, "status", OrderStatus.PACKING);
    orderRepository.save(dispatchedOrder);

    Order receivedOrder = createOrder("ORD-SL-003", "SELLER-001");
    orderRepository.save(receivedOrder);

    mockMvc.perform(get("/orders/seller/list")
            .header("X-Seller-Id", "SELLER-001")
            .param("status", "DISPATCHED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.orders[0].orderId").value("ORD-SL-002"))
        .andExpect(jsonPath("$.data.orders[0].status").value("DISPATCHED"))
        .andExpect(jsonPath("$.data.orders[0].canCancel").value(false));
  }

  private Order createOrder(String orderId, String sellerId) {
    return Order.create(
        orderId,
        LocalDateTime.of(2026, 4, 5, 10, 0),
        sellerId,
        "TENANT-001",
        OrderChannel.MANUAL,
        List.of(
            OrderItem.create("SKU-001", 1, "상품A"),
            OrderItem.create("SKU-002", 1, "상품B")
        ),
        ShippingAddress.create("서울시 강남구 테헤란로 123", "101동 202호", "Seoul", null, "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }

  private void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to set field: " + fieldName, e);
    }
  }
}
