package com.conk.order.query.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.WhmOrderListQuery;
import com.conk.order.query.dto.WhmOrderListResponse;
import com.conk.order.query.service.WhmOrderListQueryService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/* ORD-007 창고 관리자 주문 목록 조회 컨트롤러. */
@RestController
@RequestMapping("/orders")
public class WhmOrderListQueryController {

  private final WhmOrderListQueryService whmOrderListQueryService;

  public WhmOrderListQueryController(WhmOrderListQueryService whmOrderListQueryService) {
    this.whmOrderListQueryService = whmOrderListQueryService;
  }

  /*
   * GET /orders/whm
   *
   * 요청 예시:
   *   /orders/whm?warehouseId=WH-001&status=RECEIVED&page=0&size=20
   *
   * warehouseId 는 필수. WHM 은 자신이 담당하는 창고의 주문만 조회할 수 있다.
   */
  @GetMapping("/whm")
  public ApiResponse<WhmOrderListResponse> getWhmOrders(
      @RequestParam String warehouseId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    WhmOrderListQuery query = new WhmOrderListQuery();
    query.setWarehouseId(warehouseId);
    query.setStatus(status);
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    query.setPage(page);
    query.setSize(size);

    return ApiResponse.success(whmOrderListQueryService.getWhmOrders(query));
  }
}