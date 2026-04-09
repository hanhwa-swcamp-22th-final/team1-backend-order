package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.CreateOrderRequest;
import com.conk.order.command.application.dto.CreateOrderResponse;
import com.conk.order.command.application.service.CreateOrderService;
import com.conk.order.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* ORD-002 셀러 단건 주문 등록 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class CreateOrderController {

  private final CreateOrderService createOrderService;

  public CreateOrderController(CreateOrderService createOrderService) {
    this.createOrderService = createOrderService;
  }

  /*
   * POST /orders/seller/manual
   *
   * sellerId 는 NGINX 가 JWT 검증 후 주입하는 X-User-Id 헤더에서 추출한다.
   * 클라이언트가 body 에 sellerId 를 직접 보내는 방식은 위조 가능하므로 사용하지 않는다.
   */
  @PostMapping("/seller/manual")
  public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
      @RequestHeader("X-User-Id") String sellerId,
      @Valid @RequestBody CreateOrderRequest request) {
    CreateOrderResponse response = createOrderService.create(request, sellerId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("주문이 등록되었습니다.", response));
  }
}
