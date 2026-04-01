package com.conk.order.query.controller;

import com.conk.order.query.dto.ApiResponse;
import com.conk.order.query.dto.OutboundStatsResponse;
import com.conk.order.query.service.OutboundStatsQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* ORD-001 출고 통계 조회 컨트롤러. */
@RestController
@RequestMapping("/orders")
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
