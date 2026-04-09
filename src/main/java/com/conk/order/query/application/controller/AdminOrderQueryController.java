package com.conk.order.query.application.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.AdminOrderListQuery;
import com.conk.order.query.application.dto.AdminOrderListResponse;
import com.conk.order.query.application.dto.OrderDetailResponse;
import com.conk.order.query.application.service.AdminOrderListQueryService;
import com.conk.order.query.application.service.OrderDetailQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 관리자 주문 조회 컨트롤러.
 *
 * 관리자(masterAdmin) Actor 가 전체 셀러의 주문을 조회하는 엔드포인트를 묶는다.
 *   - GET /orders/list       : 관리자 주문 목록 (ORD-005, sellerId 옵셔널)
 *   - GET /orders/{orderId}  : 주문 단건 상세
 *
 * /list 와 /{orderId} 는 Spring 라우팅에서 literal 매칭이 path variable 보다 우선하므로
 * 같은 @RequestMapping("/orders") 아래에서도 충돌하지 않는다.
 */
@RestController
@RequestMapping("/orders")
public class AdminOrderQueryController {

  private final AdminOrderListQueryService adminOrderListQueryService;
  private final OrderDetailQueryService orderDetailQueryService;

  public AdminOrderQueryController(
      AdminOrderListQueryService adminOrderListQueryService,
      OrderDetailQueryService orderDetailQueryService) {
    this.adminOrderListQueryService = adminOrderListQueryService;
    this.orderDetailQueryService = orderDetailQueryService;
  }

  /*
   * GET /orders/list — 관리자 주문 목록을 조회한다.
   *
   * 요청 예시:
   *   /orders/list?sellerId=SELLER-001&status=RECEIVED&startDate=2026-04-01&page=0&size=20
   *
   * sellerId 가 없으면 전체 셀러의 주문을 조회한다 (masterAdmin 용).
   */
  @GetMapping("/list")
  public ResponseEntity<ApiResponse<AdminOrderListResponse>> getAdminOrders(
      @RequestParam(required = false) String sellerId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    AdminOrderListQuery query = new AdminOrderListQuery();
    query.setSellerId(sellerId);
    query.setStatus(status);
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    query.setPage(page);
    query.setSize(size);

    return ResponseEntity.ok(ApiResponse.success(adminOrderListQueryService.getAdminOrders(query)));
  }

  /* GET /orders/{orderId} — 주문 단건 상세를 조회한다. */
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(
      @PathVariable String orderId) {
    OrderDetailResponse response = orderDetailQueryService.getOrderDetail(orderId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
