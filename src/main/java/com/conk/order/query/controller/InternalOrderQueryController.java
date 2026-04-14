package com.conk.order.query.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.response.InternalOrderShipmentResponse;
import com.conk.order.query.dto.response.InternalOrderSummaryResponse;
import com.conk.order.query.service.InternalOrderQueryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS 내부 연동용 주문 조회 컨트롤러다.
 */
@RestController
@RequestMapping("/orders/internal")
public class InternalOrderQueryController {

  private final InternalOrderQueryService internalOrderQueryService;

  public InternalOrderQueryController(InternalOrderQueryService internalOrderQueryService) {
    this.internalOrderQueryService = internalOrderQueryService;
  }

  @GetMapping("/pending")
  public ResponseEntity<ApiResponse<List<InternalOrderSummaryResponse>>> getPendingOrders() {
    return ResponseEntity.ok(ApiResponse.success(internalOrderQueryService.getPendingOrders()));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<InternalOrderSummaryResponse>> getOrder(
      @PathVariable String orderId) {
    return ResponseEntity.ok(ApiResponse.success(internalOrderQueryService.getOrder(orderId)));
  }

  @GetMapping("/{orderId}/shipment")
  public ResponseEntity<ApiResponse<InternalOrderShipmentResponse>> getShipment(
      @PathVariable String orderId) {
    return ResponseEntity.ok(ApiResponse.success(internalOrderQueryService.getShipment(orderId)));
  }
}
