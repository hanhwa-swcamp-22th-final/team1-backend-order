package com.conk.order.command.controller;

import com.conk.order.command.dto.CreateOrderRequest;
import com.conk.order.command.dto.CreateOrderResponse;
import com.conk.order.command.service.CreateOrderService;
import com.conk.order.query.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/* ORD-002 셀러 단건 주문 등록 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class CreateOrderController {

  private final CreateOrderService createOrderService;

  public CreateOrderController(CreateOrderService createOrderService) {
    this.createOrderService = createOrderService;
  }

  @PostMapping("/seller/manual")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    CreateOrderResponse response = createOrderService.create(request);
    return ApiResponse.created("주문이 등록되었습니다.", response);
  }
}
