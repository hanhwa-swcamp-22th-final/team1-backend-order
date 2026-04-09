package com.conk.order.command.application.controller;

import com.conk.order.command.application.dto.BulkCreateOrderResponse;
import com.conk.order.command.application.dto.BulkValidateResponse;
import com.conk.order.command.application.service.BulkCreateOrderService;
import com.conk.order.command.application.service.BulkValidateOrderService;
import com.conk.order.common.dto.ApiResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/*
 * 엑셀 일괄 주문 등록 Command 컨트롤러.
 *
 * 엑셀 업로드 운영(템플릿 다운로드 → 사전 검증 → 일괄 등록) 세 엔드포인트를 묶는다.
 *   - GET  /orders/seller/bulk/template : 업로드용 12컬럼 템플릿 다운로드
 *   - POST /orders/seller/bulk/validate : 저장 없이 행별 검증 결과 반환
 *   - POST /orders/seller/bulk          : 부분 저장 정책으로 일괄 등록 (ORD-003)
 *
 * 부분 저장 정책이므로 일부 행 실패에도 200 OK 를 반환하고,
 * 성공/실패 건수와 실패 행 상세는 data 에 담아 응답한다.
 */
@RestController
@RequestMapping("/orders/seller/bulk")
public class BulkOrderCommandController {

  /* 엑셀 헤더 컬럼 순서. BulkCreateOrderService.buildOrder() 와 일치해야 한다. */
  private static final String[] TEMPLATE_HEADERS = {
      "주문일시(yyyy-MM-dd HH:mm:ss)",
      "SKU",
      "수량",
      "상품명",
      "수령인",
      "수령인 연락처",
      "기본주소",
      "상세주소",
      "도시",
      "주/지역",
      "우편번호",
      "메모"
  };

  private final BulkCreateOrderService bulkCreateOrderService;
  private final BulkValidateOrderService bulkValidateOrderService;

  public BulkOrderCommandController(
      BulkCreateOrderService bulkCreateOrderService,
      BulkValidateOrderService bulkValidateOrderService) {
    this.bulkCreateOrderService = bulkCreateOrderService;
    this.bulkValidateOrderService = bulkValidateOrderService;
  }

  /* GET /orders/seller/bulk/template — 주문 업로드용 엑셀 템플릿을 다운로드한다. */
  @GetMapping("/template")
  public ResponseEntity<byte[]> downloadTemplate() throws IOException {
    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("주문");
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
        headerRow.createCell(i).setCellValue(TEMPLATE_HEADERS[i]);
      }

      workbook.write(out);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION,
              "attachment; filename=order_upload_template.xlsx")
          .contentType(MediaType.parseMediaType(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(out.toByteArray());
    }
  }

  /* POST /orders/seller/bulk/validate — 엑셀 파일을 DB 저장 없이 검증한다. */
  @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<BulkValidateResponse>> validate(
      @RequestParam("file") MultipartFile file) {
    BulkValidateResponse response = bulkValidateOrderService.validate(file);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  /* POST /orders/seller/bulk — 엑셀 파일을 부분 저장 정책으로 일괄 등록한다. */
  @PostMapping
  public ResponseEntity<ApiResponse<BulkCreateOrderResponse>> bulkCreate(
      @RequestHeader("X-User-Id") String sellerId,
      @RequestParam MultipartFile file) {
    BulkCreateOrderResponse response = bulkCreateOrderService.create(file, sellerId);
    return ResponseEntity.ok(ApiResponse.created("일괄 주문이 등록되었습니다.", response));
  }
}
