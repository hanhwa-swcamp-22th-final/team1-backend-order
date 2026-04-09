package com.conk.order.command.application.controller;

import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

/*
 * 엑셀 업로드용 템플릿 다운로드 컨트롤러.
 *
 * GET /orders/seller/bulk/template
 * BulkCreateOrderService 가 기대하는 12컬럼 헤더가 포함된
 * 빈 xlsx 파일을 반환한다.
 */
@RestController
@RequestMapping("/orders/seller/bulk")
public class BulkOrderTemplateController {

  /* 엑셀 헤더 컬럼 순서. BulkCreateOrderService.buildOrder() 와 일치해야 한다. */
  private static final String[] HEADERS = {
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

  /* 주문 업로드용 엑셀 템플릿을 다운로드한다. */
  @GetMapping("/template")
  public ResponseEntity<byte[]> downloadTemplate() throws IOException {
    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

      Sheet sheet = workbook.createSheet("주문");
      Row headerRow = sheet.createRow(0);
      for (int i = 0; i < HEADERS.length; i++) {
        headerRow.createCell(i).setCellValue(HEADERS[i]);
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
}
