package com.conk.order.query.application.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.OutboundStatsResponse;
import com.conk.order.query.application.service.OutboundStatsQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* ORD-001 출고 통계 조회 컨트롤러. */
@RestController                // Http 요청을 처리하는 클래스
@RequestMapping("/orders")  // 모든 메서드의 기본 경로가 /orders
public class OutboundStatsQueryController {

  // 필드 선언
  private final OutboundStatsQueryService outboundStatsQueryService;

  // 메서드
  public OutboundStatsQueryController(OutboundStatsQueryService outboundStatsQueryService) {
    // 생성자
    this.outboundStatsQueryService = outboundStatsQueryService;
  }

  @GetMapping("/outbound/stats") // GET /orders/outbound/stats 에 반응
  public ResponseEntity<ApiResponse<OutboundStatsResponse>> getOutboundStats() {
    return ResponseEntity.ok(ApiResponse.success(outboundStatsQueryService.getOutboundStats()));
  }
}
