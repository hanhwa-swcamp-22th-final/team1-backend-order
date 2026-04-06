package com.conk.order.query.application.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.application.dto.SellerOrderListQuery;
import com.conk.order.query.application.dto.SellerOrderListResponse;
import com.conk.order.query.application.service.SellerOrderListQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* ORD-004 셀러 주문 목록 조회 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class SellerOrderListQueryController {

  private final SellerOrderListQueryService sellerOrderListQueryService;

  public SellerOrderListQueryController(SellerOrderListQueryService sellerOrderListQueryService) {
    this.sellerOrderListQueryService = sellerOrderListQueryService;
  }

  /*
   * GET /orders/seller/list
   *
   * 요청 예시:
   *   /orders/seller/list?sellerId=SELLER-001&status=RECEIVED&startDate=2026-04-01&page=0&size=20
   *
   * @RequestParam String sellerId          → 필수. 없으면 400 자동 반환.
   * @RequestParam(required = false) ...    → 선택. null 이면 서비스에서 필터 미적용.
   * @DateTimeFormat(iso = DATE)            → "2026-04-01" 형식의 문자열을 LocalDate 로 변환.
   * @RequestParam(defaultValue = "0")      → 파라미터 없을 때 기본값 사용.
   */
  @GetMapping("/seller/list")
  public ResponseEntity<ApiResponse<SellerOrderListResponse>> getSellerOrders(
      @RequestParam String sellerId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    /* 각 파라미터를 쿼리 객체 하나로 묶어서 서비스에 넘긴다. */
    SellerOrderListQuery query = new SellerOrderListQuery();
    query.setSellerId(sellerId);
    query.setStatus(status);
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    query.setPage(page);
    query.setSize(size);

    return ResponseEntity.ok(ApiResponse.success(sellerOrderListQueryService.getSellerOrders(query)));
  }
}
