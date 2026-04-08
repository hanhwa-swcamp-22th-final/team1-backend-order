  package com.conk.order.query.application.controller;

  import static org.hamcrest.CoreMatchers.nullValue;
  import static org.mockito.BDDMockito.given;
  import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
  import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

  import com.conk.order.query.application.dto.OutboundStatsResponse;
  import com.conk.order.query.application.service.OutboundStatsQueryService;
  import org.junit.jupiter.api.Test;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
  import org.springframework.test.context.bean.override.mockito.MockitoBean;
  import org.springframework.test.web.servlet.MockMvc;

  /* ORD-001 출고 통계 컨트롤러 테스트. HTTP 요청·응답 형식을 검증한다. */
  @WebMvcTest(OutboundStatsQueryController.class)
  public class OutboundStatsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OutboundStatsQueryService outboundStatsQueryService;

    /* GET /orders/outbound/stats — 평일 정상 응답 형식을 확인한다. */
    @Test
    void getOutboundStats_returnsSuccessWithData() throws Exception {
      given(outboundStatsQueryService.getOutboundStats())
          .willReturn(OutboundStatsResponse.builder()
              .pendingOutboundCount(5)
              .trend("+2")
              .trendLabel("전 영업일 대비")
              .trendType("up")
              .build());

      mockMvc.perform(get("/orders/outbound/stats"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.pendingOutboundCount").value(5))
          .andExpect(jsonPath("$.data.trend").value("+2"))
          .andExpect(jsonPath("$.data.trendLabel").value("전 영업일 대비"))
          .andExpect(jsonPath("$.data.trendType").value("up"));
    }

    /* GET /orders/outbound/stats — 주말에는 추이 필드가 null 인지 확인한다. */
    @Test
    void getOutboundStats_onWeekend_returnsTrendFieldsAsNull() throws Exception {
      given(outboundStatsQueryService.getOutboundStats())
          .willReturn(OutboundStatsResponse.builder()
              .pendingOutboundCount(3)
              .build());

      mockMvc.perform(get("/orders/outbound/stats"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.data.pendingOutboundCount").value(3))
          .andExpect(jsonPath("$.data.trend", nullValue()))
          .andExpect(jsonPath("$.data.trendLabel", nullValue()))
          .andExpect(jsonPath("$.data.trendType", nullValue()));
    }
  }