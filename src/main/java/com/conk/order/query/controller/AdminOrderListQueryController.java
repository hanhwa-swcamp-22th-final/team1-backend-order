package com.conk.order.query.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.AdminOrderListQuery;
import com.conk.order.query.dto.AdminOrderListResponse;
import com.conk.order.query.service.AdminOrderListQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* ORD-005 관리자 주문 목록 조회 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class AdminOrderListQueryController {

  private final AdminOrderListQueryService adminOrderListQueryService;

  public AdminOrderListQueryController(AdminOrderListQueryService adminOrderListQueryService) {
    this.adminOrderListQueryService = adminOrderListQueryService;
  }

  /*
   * GET /orders/list
   *
   * 요청 예시:
   *   /orders/list?sellerId=SELLER-001&status=RECEIVED&startDate=2026-04-01&page=0&size=20
   *
   * ORD-004 와 달리 sellerId 가 required=false 다.
   * masterAdmin 은 파라미터 없이 요청하면 전체 셀러의 주문을 모두 조회한다.
   */
  @GetMapping("/list")
  public ApiResponse<AdminOrderListResponse> getAdminOrders(
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

    return ApiResponse.success(adminOrderListQueryService.getAdminOrders(query));
  }
}
