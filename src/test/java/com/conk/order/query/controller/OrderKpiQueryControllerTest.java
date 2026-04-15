package com.conk.order.query.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.query.dto.response.OrderKpiResponse;
import com.conk.order.query.service.OrderDashboardQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-006 주문 KPI 집계 컨트롤러 단위 테스트.
 *
 * @WebMvcTest: MVC 레이어만 로드한다.
 * @MockitoBean: OrderDashboardQueryService 를 Mockito 가짜 객체로 대체한다.
 *
 * 이 테스트가 검증하는 것:
 *   - 파라미터 없이 요청해도 200 OK 인가 (전체 기간 집계)
 *   - JSON 응답 구조가 success/data 형식인가
 *   - 응답 필드(totalCount, 상태별 건수)가 올바르게 직렬화되는가
 */
@WebMvcTest(OrderDashboardQueryController.class)
class OrderKpiQueryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private OrderDashboardQueryService orderDashboardQueryService;

  /* 파라미터 없이 요청해도 200 OK 와 KPI 응답을 반환한다. */
  @Test
  void getKpi_returnsOkWithKpiData() throws Exception {
    given(orderDashboardQueryService.getKpi(any()))
        .willReturn(new OrderKpiResponse(10, 6, 2, 2));

    mockMvc.perform(get("/orders/kpi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.todayTotal").value(10))
        .andExpect(jsonPath("$.data.pendingCount").value(6))
        .andExpect(jsonPath("$.data.pickingCount").value(2))
        .andExpect(jsonPath("$.data.shippedCount").value(2));
  }

  @Test
  void getKpi_returnsOk_withDateFilter() throws Exception {
    given(orderDashboardQueryService.getKpi(any()))
        .willReturn(new OrderKpiResponse(5, 5, 0, 0));

    mockMvc.perform(get("/orders/kpi")
            .param("startDate", "2026-04-01")
            .param("endDate", "2026-04-05"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.todayTotal").value(5));
  }
}
