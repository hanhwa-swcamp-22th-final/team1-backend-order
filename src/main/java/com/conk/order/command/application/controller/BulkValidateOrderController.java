package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.BulkValidateResponse;
import com.conk.order.command.application.service.BulkValidateOrderService;
import com.conk.order.common.dto.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
 * 엑셀 일괄 등록 사전 검증 컨트롤러.
 *
 * POST /orders/seller/bulk/validate
 * DB 저장 없이 엑셀 파일의 각 행을 검증하여 오류 목록을 반환한다.
 */
@RestController
@RequestMapping("/orders/seller/bulk")
public class BulkValidateOrderController {

  private final BulkValidateOrderService bulkValidateOrderService;

  public BulkValidateOrderController(BulkValidateOrderService bulkValidateOrderService) {
    this.bulkValidateOrderService = bulkValidateOrderService;
  }

  /* 엑셀 파일을 검증하고 결과를 반환한다. */
  @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<BulkValidateResponse>> validate(
      @RequestParam("file") MultipartFile file) {
    BulkValidateResponse response = bulkValidateOrderService.validate(file);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
