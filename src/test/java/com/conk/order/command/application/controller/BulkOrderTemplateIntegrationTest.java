package com.conk.order.command.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/*
 * 엑셀 템플릿 다운로드 통합 테스트.
 *
 * 검증 대상:
 *   - 200 OK 와 xlsx 파일 반환
 *   - Content-Disposition 헤더에 파일명 포함
 *   - 헤더 행에 12개 컬럼이 포함됨
 */
@SpringBootTest
@AutoConfigureMockMvc
class BulkOrderTemplateIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  /* 템플릿 다운로드 시 12컬럼 헤더가 포함된 xlsx 를 반환한다. */
  @Test
  void downloadTemplate_returnsXlsxWithHeaders() throws Exception {
    MvcResult result = mockMvc.perform(get("/orders/seller/bulk/template"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Disposition",
            "attachment; filename=order_upload_template.xlsx"))
        .andReturn();

    byte[] body = result.getResponse().getContentAsByteArray();
    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(body))) {
      Sheet sheet = workbook.getSheetAt(0);
      Row headerRow = sheet.getRow(0);

      assertThat(headerRow.getPhysicalNumberOfCells()).isEqualTo(12);
      assertThat(headerRow.getCell(0).getStringCellValue())
          .contains("주문일시");
      assertThat(headerRow.getCell(1).getStringCellValue())
          .isEqualTo("SKU");
    }
  }
}
