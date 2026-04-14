package com.conk.order.query.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.response.CurrentRevenueResponse;
import com.conk.order.query.dto.response.MonthlyRevenuePointResponse;
import com.conk.order.query.dto.request.OrderKpiQuery;
import com.conk.order.query.dto.response.OrderKpiResponse;
import com.conk.order.query.dto.response.OutboundStatsResponse;
import com.conk.order.query.dto.response.SellerRevenueResponse;
import com.conk.order.query.service.OrderDashboardQueryService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 주문 대시보드 집계 조회 컨트롤러.
 *
 *   - GET /orders/outbound/stats : 출고 통계 (ORD-001)
 *   - GET /orders/kpi            : 주문 KPI 집계 (ORD-006)
 *
 * 관리자 대시보드 화면에서 사용하는 집계·통계 조회를 한곳에 묶는다.
 */
@RestController
@RequestMapping("/orders")
public class OrderDashboardQueryController {

  private final OrderDashboardQueryService orderDashboardQueryService;

  public OrderDashboardQueryController(OrderDashboardQueryService orderDashboardQueryService) {
    this.orderDashboardQueryService = orderDashboardQueryService;
  }

  /* GET /orders/outbound/stats — 출고 통계를 조회한다. */
  @GetMapping("/outbound/stats")
  public ResponseEntity<ApiResponse<OutboundStatsResponse>> getOutboundStats() {
    return ResponseEntity.ok(ApiResponse.success(orderDashboardQueryService.getOutboundStats()));
  }

  /*
   * GET /orders/kpi — 주문 KPI 집계를 조회한다.
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

    return ResponseEntity.ok(ApiResponse.success(orderDashboardQueryService.getKpi(query)));
  }

  @GetMapping("/revenue/current")
  public ResponseEntity<ApiResponse<CurrentRevenueResponse>> getCurrentRevenue() {
    return ResponseEntity.ok(ApiResponse.success(orderDashboardQueryService.getCurrentRevenue()));
  }

  @GetMapping("/revenue/monthly")
  public ResponseEntity<ApiResponse<List<MonthlyRevenuePointResponse>>> getMonthlyRevenue() {
    return ResponseEntity.ok(ApiResponse.success(orderDashboardQueryService.getMonthlyRevenue()));
  }

  @GetMapping("/revenue/sellers")
  public ResponseEntity<ApiResponse<List<SellerRevenueResponse>>> getSellerRevenue() {
    return ResponseEntity.ok(ApiResponse.success(orderDashboardQueryService.getSellerRevenue()));
  }
}
