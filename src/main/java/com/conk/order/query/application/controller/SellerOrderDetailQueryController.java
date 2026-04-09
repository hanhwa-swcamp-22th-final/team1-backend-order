package com.conk.order.query.application.controller;

import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.SellerOrderDetailResponse;
import com.conk.order.query.application.service.SellerOrderDetailQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 셀러 주문 상세 조회 컨트롤러.
 *
 * GET /orders/seller/{orderId}
 * 셀러 본인의 주문만 조회 가능. canCancel 필드를 포함한 응답을 반환한다.
 */
@RestController
@RequestMapping("/orders/seller")
public class SellerOrderDetailQueryController {

  private final SellerOrderDetailQueryService sellerOrderDetailQueryService;

  public SellerOrderDetailQueryController(SellerOrderDetailQueryService service) {
    this.sellerOrderDetailQueryService = service;
  }

  /* 셀러 주문 상세를 조회한다. */
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<SellerOrderDetailResponse>> getDetail(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    SellerOrderDetailResponse response = sellerOrderDetailQueryService.getDetail(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
