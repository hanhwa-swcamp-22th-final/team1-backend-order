package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.BulkCreateOrderResponse;
import com.conk.order.command.application.service.BulkCreateOrderService;
import com.conk.order.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
 * ORD-003 엑셀 일괄 주문 등록 컨트롤러.
 *
 * POST /orders/seller/bulk
 * multipart/form-data: file (xlsx), sellerId (String)
 *
 * 부분 저장 정책이므로 일부 행 실패에도 200 OK 를 반환한다.
 * 성공/실패 건수와 실패 행 상세는 data 에 담아 응답한다.
 */
@RestController
@RequestMapping("/orders")
public class BulkCreateOrderController {

  private final BulkCreateOrderService bulkCreateOrderService;

  public BulkCreateOrderController(BulkCreateOrderService bulkCreateOrderService) {
    this.bulkCreateOrderService = bulkCreateOrderService;
  }

  @PostMapping("/seller/bulk")
  public ResponseEntity<ApiResponse<BulkCreateOrderResponse>> bulkCreate(
      @RequestParam String sellerId,
      @RequestParam MultipartFile file
  ) {
    BulkCreateOrderResponse response = bulkCreateOrderService.create(file, sellerId);
    return ResponseEntity.ok(ApiResponse.created("일괄 주문이 등록되었습니다.", response));
  }
}
