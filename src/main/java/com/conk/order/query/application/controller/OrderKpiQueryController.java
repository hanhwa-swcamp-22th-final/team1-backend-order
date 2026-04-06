package com.conk.order.query.application.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.OrderKpiQuery;
import com.conk.order.query.application.dto.OrderKpiResponse;
import com.conk.order.query.application.service.OrderKpiQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* ORD-006 주문 KPI 집계 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class OrderKpiQueryController {

  private final OrderKpiQueryService orderKpiQueryService;

  public OrderKpiQueryController(OrderKpiQueryService orderKpiQueryService) {
    this.orderKpiQueryService = orderKpiQueryService;
  }

  /*
   * GET /orders/kpi
   *
   * 요청 예시:
   *   /orders/kpi
   *   /orders/kpi?startDate=2026-04-01&endDate=2026-04-05
   *
   * 날짜 파라미터가 없으면 전체 기간 집계.
   */
  @GetMapping("/kpi")
  public ResponseEntity<ApiResponse<OrderKpiResponse>> getKpi(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    OrderKpiQuery query = new OrderKpiQuery();
    query.setStartDate(startDate);
    query.setEndDate(endDate);

    return ResponseEntity.ok(ApiResponse.success(orderKpiQueryService.getKpi(query)));
  }
}
