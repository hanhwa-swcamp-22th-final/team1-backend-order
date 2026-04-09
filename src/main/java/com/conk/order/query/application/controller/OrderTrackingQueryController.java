package com.conk.order.query.application.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.OrderTrackingResponse;
import com.conk.order.query.application.service.OrderTrackingQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 주문 상태 트래킹 컨트롤러.
 *
 * GET /orders/seller/{orderId}/tracking
 * 셀러 본인 주문의 상태 변경 이력을 시간순으로 반환한다.
 */
@RestController
@RequestMapping("/orders/seller")
public class OrderTrackingQueryController {

  private final OrderTrackingQueryService orderTrackingQueryService;

  public OrderTrackingQueryController(OrderTrackingQueryService orderTrackingQueryService) {
    this.orderTrackingQueryService = orderTrackingQueryService;
  }

  /* 주문 상태 변경 이력을 조회한다. */
  @GetMapping("/{orderId}/tracking")
  public ResponseEntity<ApiResponse<OrderTrackingResponse>> getTracking(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    OrderTrackingResponse response = orderTrackingQueryService.getTracking(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
