package com.conk.order.command.application.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.repository.OrderRepository;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * ORD-003 엑셀 일괄 주문 등록 통합 테스트.
 *
 * 전체 스택(Controller → Service → JPA → H2)을 실제로 실행해
 * 엑셀 파일 파싱부터 DB 저장까지의 경로를 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BulkCreateOrderIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private OrderRepository orderRepository;

  /*
   * 유효한 2행짜리 엑셀 업로드 시 2건 모두 DB에 저장된다.
   * successCount=2, failedRows 빈 배열, 실제 DB 저장 수=2 를 확인한다.
   */
  @Test
  void bulkCreate_savesAllOrders_whenAllRowsAreValid() throws Exception {
    MockMultipartFile file = excelFile(
        new String[]{"2026-04-07 10:00:00", "SKU-001", "2", "상품A",
            "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "", "Seoul", "", "06236", ""},
        new String[]{"2026-04-07 11:00:00", "SKU-002", "1", "상품B",
            "김철수", "010-9876-5432", "서울시 서초구 반포대로 456", "", "Seoul", "", "06500", ""}
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .header("X-User-Id", "SELLER-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.successCount").value(2))
        .andExpect(jsonPath("$.data.failedRows").isEmpty());

    assertThat(orderRepository.count()).isEqualTo(2);
  }

  /*
   * 유효한 행과 SKU 없는 행이 섞여 있으면 유효한 행만 DB에 저장된다.
   * successCount=1, failedRows 1건, 실제 DB 저장 수=1 을 확인한다.
   */
  @Test
  void bulkCreate_savesOnlyValidRows_whenSomeRowsFail() throws Exception {
    MockMultipartFile file = excelFile(
        new String[]{"2026-04-07 10:00:00", "SKU-001", "2", "",
            "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "", "Seoul", "", "06236", ""},
        new String[]{"2026-04-07 11:00:00", "", "1", "",  /* SKU 없음 → 실패 */
            "김철수", "010-9876-5432", "서울시 서초구 반포대로 456", "", "Seoul", "", "06500", ""}
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .header("X-User-Id", "SELLER-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.successCount").value(1))
        .andExpect(jsonPath("$.data.failedRows").isNotEmpty());

    assertThat(orderRepository.count()).isEqualTo(1);
  }

  /*
   * xlsx 가 아닌 파일을 업로드하면 400 Bad Request 를 반환한다.
   */
  @Test
  void bulkCreate_returnsBadRequest_whenFileIsNotXlsx() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
        "file", "orders.csv",
        "text/csv",
        "not,an,xlsx,file".getBytes()
    );

    mockMvc.perform(multipart("/orders/seller/bulk")
            .file(file)
            .header("X-User-Id", "SELLER-001")
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /*
   * 데이터 행 배열을 받아 xlsx 파일 바이트로 변환한다.
   * 첫 행은 헤더, 이후 가변 인자 rows 를 데이터 행으로 추가한다.
   */
  private MockMultipartFile excelFile(String[]... rows) throws Exception {
    try (XSSFWorkbook wb = new XSSFWorkbook()) {
      XSSFSheet sheet = wb.createSheet();

      /* 헤더 행 */
      XSSFRow header = sheet.createRow(0);
      String[] headers = {"주문번호", "주문일시", "SKU", "수량", "상품명",
          "수령인", "연락처", "주소1", "주소2", "도시", "주/지역", "우편번호", "메모"};
      for (int i = 0; i < headers.length; i++) {
        header.createCell(i).setCellValue(headers[i]);
      }

      /* 데이터 행 */
      for (int r = 0; r < rows.length; r++) {
        XSSFRow row = sheet.createRow(r + 1);
        for (int c = 0; c < rows[r].length; c++) {
          row.createCell(c).setCellValue(rows[r][c]);
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wb.write(out);
      return new MockMultipartFile(
          "file", "orders.xlsx",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          out.toByteArray()
      );
    }
  }
}