package com.conk.order.query.application.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.OrderDetailResponse;
import com.conk.order.query.application.service.OrderDetailQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 주문 단건 상세 조회 컨트롤러.
 *
 * GET /orders/{orderId}
 * 응답: { "success": true, "data": { orderId, status, items, ... } }
 */
@RestController
@RequestMapping("/orders")
public class OrderDetailQueryController {

  private final OrderDetailQueryService orderDetailQueryService;

  public OrderDetailQueryController(OrderDetailQueryService orderDetailQueryService) {
    this.orderDetailQueryService = orderDetailQueryService;
  }

  /* 주문 상세를 조회한다. */
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
      @PathVariable String orderId) {
    OrderDetailResponse response = orderDetailQueryService.getOrderDetail(orderId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
