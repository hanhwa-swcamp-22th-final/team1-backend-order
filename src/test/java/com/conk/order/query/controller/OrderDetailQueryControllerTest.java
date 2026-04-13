package com.conk.order.query.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import com.conk.order.query.dto.response.OrderDetailResponse;
import com.conk.order.query.service.AdminOrderQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * 주문 단건 조회 컨트롤러 단위 테스트.
 *
 * 검증 대상:
 *   - 정상 조회 시 200 OK + 상세 데이터
 *   - 존재하지 않는 주문 시 404
 */
@WebMvcTest(AdminOrderQueryController.class)
class OrderDetailQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AdminOrderQueryService adminOrderQueryService;

  /* 정상 조회 시 200 과 주문 상세를 반환한다. */
  @Test
  void getOrderDetail_returnsOk() throws Exception {
    Order order = Order.create(
        "ORD-001", LocalDateTime.of(2026, 4, 9, 10, 0), "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 2, "상품A")),
        ShippingAddress.create("123 Main St", "Apt 4", "LA", "CA", "90001"),
        "홍길동", "010-1234-5678", "빠른배송"
    );
    given(adminOrderQueryService.getOrderDetail("ORD-001"))
        .willReturn(OrderDetailResponse.from(order));

    mockMvc.perform(get("/orders/ORD-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orderId").value("ORD-001"))
        .andExpect(jsonPath("$.data.status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.street1").value("123 Main St"))
        .andExpect(jsonPath("$.data.items[0].sku").value("SKU-001"))
        .andExpect(jsonPath("$.data.items[0].quantity").value(2));
  }

  /* 존재하지 않는 주문 시 404 를 반환한다. */
  @Test
  void getOrderDetail_returns404_whenNotFound() throws Exception {
    willThrow(new BusinessException(ErrorCode.ORDER_NOT_FOUND))
        .given(adminOrderQueryService).getOrderDetail("NONE");

    mockMvc.perform(get("/orders/NONE"))
        .andExpect(status().isNotFound());
  }
}
