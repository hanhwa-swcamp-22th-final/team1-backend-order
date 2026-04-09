package com.conk.order.query.application.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.query.application.dto.OrderKpiResponse;
import com.conk.order.query.application.service.OrderKpiQueryService;
import com.conk.order.query.application.service.OutboundStatsQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/*
 * ORD-006 주문 KPI 집계 컨트롤러 단위 테스트.
 *
 * @WebMvcTest: MVC 레이어만 로드한다.
 * @MockitoBean: OrderKpiQueryService 를 Mockito 가짜 객체로 대체한다.
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
  private OrderKpiQueryService orderKpiQueryService;

  /* 병합된 OrderDashboardQueryController 가 함께 의존하는 서비스. 이 테스트에서는 호출되지 않지만 컨텍스트 로딩을 위해 필요하다. */
  @MockitoBean
  private OutboundStatsQueryService outboundStatsQueryService;

  /* 파라미터 없이 요청해도 200 OK 와 KPI 응답을 반환한다. */
  @Test
  void getKpi_returnsOkWithKpiData() throws Exception {
    given(orderKpiQueryService.getKpi(any()))
        .willReturn(new OrderKpiResponse(10, 3, 2, 1, 1, 1, 0, 2, 1));

    mockMvc.perform(get("/orders/kpi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.totalCount").value(10))
        .andExpect(jsonPath("$.data.receivedCount").value(3))
        .andExpect(jsonPath("$.data.allocatedCount").value(2))
        .andExpect(jsonPath("$.data.outboundInstructedCount").value(1))
        .andExpect(jsonPath("$.data.pickingCount").value(1))
        .andExpect(jsonPath("$.data.packingCount").value(1))
        .andExpect(jsonPath("$.data.outboundPendingCount").value(0))
        .andExpect(jsonPath("$.data.outboundCompletedCount").value(2))
        .andExpect(jsonPath("$.data.canceledCount").value(1));
  }

  /* 날짜 파라미터를 전달해도 200 OK 를 반환한다. */
  @Test
  void getKpi_returnsOk_withDateFilter() throws Exception {
    given(orderKpiQueryService.getKpi(any()))
        .willReturn(new OrderKpiResponse(5, 5, 0, 0, 0, 0, 0, 0, 0));

    mockMvc.perform(get("/orders/kpi")
            .param("startDate", "2026-04-01")
            .param("endDate", "2026-04-05"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalCount").value(5));
  }
}
