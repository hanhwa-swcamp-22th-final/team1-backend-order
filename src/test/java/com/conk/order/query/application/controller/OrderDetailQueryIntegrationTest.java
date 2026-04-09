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
 * 주문 단건 조회 통합 테스트.
 *
 * Controller → Service → Repository → H2 DB 전체 스택 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderDetailQueryIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /* 주문을 저장한 뒤 단건 조회하면 상세 정보가 반환된다. */
  @Test
  void getOrderDetail_returnsFullDetail() throws Exception {
    Order order = Order.create(
        "ORD-DETAIL-001", LocalDateTime.of(2026, 4, 9, 10, 0), "SELLER-001",
        OrderChannel.MANUAL,
        List.of(
            OrderItem.create("SKU-001", 2, "상품A"),
            OrderItem.create("SKU-002", 1, "상품B")
        ),
        ShippingAddress.create("123 Main St", "Apt 4", "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", "빠른배송"
    );
    orderRepository.save(order);

    mockMvc.perform(get("/orders/ORD-DETAIL-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value("ORD-DETAIL-001"))
        .andExpect(jsonPath("$.data.sellerId").value("SELLER-001"))
        .andExpect(jsonPath("$.data.status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.receiverName").value("홍길동"))
        .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
        .andExpect(jsonPath("$.data.street1").value("123 Main St"))
        .andExpect(jsonPath("$.data.street2").value("Apt 4"))
        .andExpect(jsonPath("$.data.country").value("USA"))
        .andExpect(jsonPath("$.data.memo").value("빠른배송"))
        .andExpect(jsonPath("$.data.items.length()").value(2))
        .andExpect(jsonPath("$.data.items[0].sku").value("SKU-001"))
        .andExpect(jsonPath("$.data.items[0].quantity").value(2))
        .andExpect(jsonPath("$.data.items[0].productName").value("상품A"));
  }

  /* 존재하지 않는 주문 조회 시 404 를 반환한다. */
  @Test
  void getOrderDetail_returns404_whenNotFound() throws Exception {
    mockMvc.perform(get("/orders/NONEXISTENT"))
        .andExpect(status().isNotFound());
  }
}
