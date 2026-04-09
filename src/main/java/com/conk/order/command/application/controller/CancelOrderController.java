package com.conk.order.command.application.controller;

import com.conk.order.command.application.service.CancelOrderService;
import com.conk.order.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 셀러 주문 취소 컨트롤러.
 *
 * PATCH /orders/seller/{orderId}/cancel
 * 셀러 본인의 주문만 취소할 수 있다.
 * RECEIVED, ALLOCATED 상태에서만 취소 가능.
 */
@RestController
@RequestMapping("/orders/seller")
public class CancelOrderController {

  private final CancelOrderService cancelOrderService;

  public CancelOrderController(CancelOrderService cancelOrderService) {
    this.cancelOrderService = cancelOrderService;
  }

  /* 셀러의 주문을 취소한다. */
  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancel(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    cancelOrderService.cancel(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.created("주문이 취소되었습니다.", null));
  }
}
