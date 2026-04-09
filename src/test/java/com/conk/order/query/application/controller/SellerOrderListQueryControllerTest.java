package com.conk.order.query.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.application.dto.SellerOrderListResponse;
import com.conk.order.query.application.dto.SellerOrderSummary;
import com.conk.order.query.application.service.SellerOrderListQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-004 셀러 주문 목록 조회 컨트롤러 단위 테스트.
 *
 * sellerId 는 X-User-Id 헤더에서 추출한다.
 */
@WebMvcTest(SellerOrderListQueryController.class)
class SellerOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SellerOrderListQueryService sellerOrderListQueryService;

  /* 정상 요청 시 200 OK 와 success/data 형식으로 응답한다. */
  @Test
  void getSellerOrders_returnsOkWithData() throws Exception {
    SellerOrderSummary summary = new SellerOrderSummary();
    summary.setOrderNo("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 3, 10, 0));
    summary.setStatus(OrderStatus.RECEIVED);
    summary.setOrderChannel(OrderChannel.MANUAL);
    summary.setReceiverName("홍길동");
    summary.setItemCount(2);

    given(sellerOrderListQueryService.getSellerOrders(any()))
        .willReturn(new SellerOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/seller/list")
            .header("X-User-Id", "SELLER-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"))
        .andExpect(jsonPath("$.data.orders[0].status").value("RECEIVED"))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20));
  }

  /*
   * X-User-Id 헤더가 없으면 GlobalExceptionHandler 가 401 Unauthorized 를 반환한다.
   */
  @Test
  void getSellerOrders_returnsUnauthorized_whenUserIdHeaderMissing() throws Exception {
    mockMvc.perform(get("/orders/seller/list"))
        .andExpect(status().isUnauthorized());
  }
}
