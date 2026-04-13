package com.conk.order.query.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.request.SellerOrderListQuery;
import com.conk.order.query.dto.response.OrderTrackingResponse;
import com.conk.order.query.dto.response.SellerOrderDetailResponse;
import com.conk.order.query.dto.response.SellerOrderListResponse;
import com.conk.order.query.service.SellerOrderQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 셀러 주문 조회 컨트롤러.
 *
 * 셀러 Actor 가 자신의 주문을 조회하는 엔드포인트를 한곳에 묶는다.
 *   - GET /orders/seller/list             : 주문 목록 (ORD-004)
 *   - GET /orders/seller/{orderId}        : 주문 상세 (canCancel 포함)
 *   - GET /orders/seller/{orderId}/tracking : 주문 상태 변경 이력
 *
 * sellerId 는 NGINX 가 JWT 검증 후 주입하는 X-User-Id 헤더에서 추출한다.
 */
@RestController
@RequestMapping("/orders/seller")
public class SellerOrderQueryController {

  private final SellerOrderQueryService sellerOrderQueryService;

  public SellerOrderQueryController(SellerOrderQueryService sellerOrderQueryService) {
    this.sellerOrderQueryService = sellerOrderQueryService;
  }

  /* GET /orders/seller/list — 셀러 본인의 주문 목록을 조회한다. */
  @GetMapping("/list")
  public ResponseEntity<ApiResponse<SellerOrderListResponse>> getSellerOrders(
      @RequestHeader("X-User-Id") String sellerId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    SellerOrderListQuery query = new SellerOrderListQuery();
    query.setSellerId(sellerId);
    query.setStatus(status);
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    query.setPage(page);
    query.setSize(size);

    return ResponseEntity.ok(ApiResponse.success(sellerOrderQueryService.getSellerOrders(query)));
  }

  /* GET /orders/seller/{orderId} — 셀러 본인의 주문 상세를 조회한다. */
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<SellerOrderDetailResponse>> getDetail(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    SellerOrderDetailResponse response = sellerOrderQueryService.getDetail(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /* GET /orders/seller/{orderId}/tracking — 주문 상태 변경 이력을 시간순으로 조회한다. */
  @GetMapping("/{orderId}/tracking")
  public ResponseEntity<ApiResponse<OrderTrackingResponse>> getTracking(
      @PathVariable String orderId,
      @RequestHeader("X-User-Id") String sellerId) {
    OrderTrackingResponse response = sellerOrderQueryService.getTracking(orderId, sellerId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
