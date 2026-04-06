package com.conk.order.query.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.application.dto.WhmOrderListResponse;
import com.conk.order.query.application.dto.WhmOrderSummary;
import com.conk.order.query.application.service.WhmOrderListQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-007 창고 관리자 주문 목록 조회 컨트롤러 단위 테스트.
 *
 * 이 테스트가 검증하는 것:
 *   - warehouseId 있으면 200 OK 와 success/data 형식으로 응답하는가
 *   - warehouseId 없으면 400 Bad Request 를 반환하는가 (필수 파라미터)
 */
@WebMvcTest(WhmOrderListQueryController.class)
class WhmOrderListQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private WhmOrderListQueryService whmOrderListQueryService;

  /* warehouseId 전달 시 200 OK 와 success/data 형식으로 응답한다. */
  @Test
  void getWhmOrders_returnsOkWithData() throws Exception {
    WhmOrderSummary summary = new WhmOrderSummary();
    summary.setOrderNo("ORD-001");
    summary.setOrderedAt(LocalDateTime.of(2026, 4, 5, 10, 0));
    summary.setStatus(OrderStatus.RECEIVED);
    summary.setOrderChannel(OrderChannel.MANUAL);
    summary.setReceiverName("홍길동");
    summary.setItemCount(2);

    given(whmOrderListQueryService.getWhmOrders(any()))
        .willReturn(new WhmOrderListResponse(List.of(summary), 1, 0, 20));

    mockMvc.perform(get("/orders/whm")
            .param("warehouseId", "WH-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.orders[0].orderNo").value("ORD-001"))
        .andExpect(jsonPath("$.data.totalCount").value(1))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20));
  }

  /* warehouseId 없이 요청하면 400 Bad Request 를 반환한다. */
  @Test
  void getWhmOrders_returnsBadRequest_whenWarehouseIdMissing() throws Exception {
    mockMvc.perform(get("/orders/whm"))
        .andExpect(status().isBadRequest());
  }
}
