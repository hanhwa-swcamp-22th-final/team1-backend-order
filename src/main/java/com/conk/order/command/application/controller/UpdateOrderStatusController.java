package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.UpdateOrderStatusRequest;
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
 * 주문 상태 변경 컨트롤러.
 *
 * PATCH /orders/{orderId}/status
 * 요청 바디: { "status": "PICKING" }
 * 응답: { "success": true, "message": "주문 상태가 변경되었습니다." }
 */
@RestController
@RequestMapping("/orders")
public class UpdateOrderStatusController {

  private final UpdateOrderStatusService updateOrderStatusService;

  public UpdateOrderStatusController(UpdateOrderStatusService updateOrderStatusService) {
    this.updateOrderStatusService = updateOrderStatusService;
  }

  /* 주문 상태를 변경한다. */
  @PatchMapping("/{orderId}/status")
  public ResponseEntity<ApiResponse<Void>> updateStatus(
      @PathVariable String orderId,
      @Valid @RequestBody UpdateOrderStatusRequest request) {
    updateOrderStatusService.updateStatus(orderId, request);
    return ResponseEntity.ok(ApiResponse.created("주문 상태가 변경되었습니다.", null));
  }
}
