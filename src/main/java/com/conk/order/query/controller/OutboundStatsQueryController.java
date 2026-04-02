package com.conk.order.query.controller;

import com.conk.order.query.dto.ApiResponse;
import com.conk.order.query.dto.OutboundStatsResponse;
import com.conk.order.query.service.OutboundStatsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* ORD-001 출고 통계 조회 컨트롤러. */
@RestController                // Http 요청을 처리하는 클래스
@RequestMapping("/orders")  // 모든 메서드의 기본 경로가 /orders
public class OutboundStatsQueryController {

  private final OutboundStatsQueryService outboundStatsQueryService;

  public OutboundStatsQueryController(OutboundStatsQueryService outboundStatsQueryService) {
    this.outboundStatsQueryService = outboundStatsQueryService;
  }

  @GetMapping("/outbound/stats")
  public ApiResponse<OutboundStatsResponse> getOutboundStats() {
    return ApiResponse.success(outboundStatsQueryService.getOutboundStats());
  }
}
