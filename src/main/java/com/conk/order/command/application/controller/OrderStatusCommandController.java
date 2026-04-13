package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.request.UpdateOrderStatusRequest;
import com.conk.order.command.application.service.UpdateOrderStatusService;
import com.conk.order.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 주문 상태 Command 컨트롤러.
 *
 * 창고 관리자·관리자가 주문 상태를 변경하는 엔드포인트를 모은다.
 *   - PATCH /orders/{orderId}/status : 주문 상태 변경
 *
 * 향후 출고 확정, 재할당 등 관리 측 상태 변경 기능이 추가되면 이 컨트롤러에 누적한다.
 */
@RestController
@RequestMapping("/orders")
public class OrderStatusCommandController {

  private final UpdateOrderStatusService updateOrderStatusService;

  public OrderStatusCommandController(UpdateOrderStatusService updateOrderStatusService) {
    this.updateOrderStatusService = updateOrderStatusService;
  }

  /* PATCH /orders/{orderId}/status — 주문 상태를 변경한다. */
  @PatchMapping("/{orderId}/status")
  public ResponseEntity<ApiResponse<Void>> updateStatus(
      @PathVariable String orderId,
      @Valid @RequestBody UpdateOrderStatusRequest request) {
    updateOrderStatusService.updateStatus(orderId, request);
    return ResponseEntity.ok(ApiResponse.created("주문 상태가 변경되었습니다.", null));
  }
}
