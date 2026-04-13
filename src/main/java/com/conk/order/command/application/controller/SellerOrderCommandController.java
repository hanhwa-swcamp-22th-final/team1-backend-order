package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.request.CreateOrderRequest;
import com.conk.order.command.application.dto.response.CreateOrderResponse;
import com.conk.order.command.application.service.SellerOrderCommandService;
import com.conk.order.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 셀러 주문 Command 컨트롤러.
 *
 * 셀러 Actor 가 자신의 주문을 생성·취소하는 엔드포인트를 묶는다.
 *   - POST  /orders/seller/manual         : 단건 주문 등록 (ORD-002)
 *   - PATCH /orders/seller/{orderId}/cancel : 주문 취소
 *
 * sellerId 는 NGINX 가 JWT 검증 후 주입하는 X-User-Id 헤더에서 추출한다.
 * 클라이언트가 body 에 sellerId 를 직접 보내는 방식은 위조 가능하므로 사용하지 않는다.
 */
@RestController
@RequestMapping("/orders/seller")
public class SellerOrderCommandController {

  private final SellerOrderCommandService sellerOrderCommandService;

  public SellerOrderCommandController(SellerOrderCommandService sellerOrderCommandService) {
    this.sellerOrderCommandService = sellerOrderCommandService;
  }

  /* POST /orders/seller/manual — 셀러 단건 주문을 등록한다. */
  @PostMapping("/manual")
  public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
      @RequestHeader("X-User-Id") String sellerId,
      @Valid @RequestBody CreateOrderRequest request) {
    CreateOrderResponse response = sellerOrderCommandService.create(request, sellerId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("주문이 등록되었습니다.", response));
  }

  /* PATCH /orders/seller/{orderId}/cancel — 셀러 본인의 주문을 취소한다. */
  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<ApiResponse<Void>> cancel(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    sellerOrderCommandService.cancel(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.created("주문이 취소되었습니다.", null));
  }
}
