package com.conk.order.command.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.dto.BulkCreateOrderResponse;
import com.conk.order.command.port.OrderSavePort;
import com.conk.order.common.exception.BusinessException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/*
 * ORD-003 엑셀 일괄 주문 등록 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * OrderSavePort 는 StubSavePort 로 대체한다.
 * 엑셀 파일은 Apache POI 로 인메모리 생성해 MultipartFile 로 감싼다.
 */
class BulkCreateOrderServiceTest {

  /* 유효한 2행 엑셀 → successCount=2, failedRows 없음. */
  @Test
  void create_savesAllValidRows() throws Exception {
    StubSavePort stub = new StubSavePort();
    BulkCreateOrderService service = new BulkCreateOrderService(stub);

    MultipartFile file = buildExcel(
        row("", "2026-04-05 10:00:00", "SKU-001", "2", "상품A", "홍길동", "010-1111-2222",
            "서울시 강남구 1번지", "", "Seoul", "", "06236", ""),
        row("", "2026-04-05 11:00:00", "SKU-002", "1", "", "김철수", "010-3333-4444",
            "서울시 서초구 2번지", "", "Seoul", "", "06500", "메모")
    );

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isEqualTo(2);
    assertThat(response.getFailedRows()).isEmpty();
    assertThat(stub.saved).hasSize(2);
  }

  /* 1행 유효 + 1행 SKU 누락 → successCount=1, failedRows 1건. */
  @Test
  void create_collectsFailedRows_whenSomeRowsInvalid() throws Exception {
    StubSavePort stub = new StubSavePort();
    BulkCreateOrderService service = new BulkCreateOrderService(stub);

    MultipartFile file = buildExcel(
        row("", "2026-04-05 10:00:00", "SKU-001", "1", "", "홍길동", "010-1111-2222",
            "서울시 강남구 1번지", "", "Seoul", "", "06236", ""),
        row("", "2026-04-05 11:00:00", "",        "1", "", "김철수", "010-3333-4444",
            "서울시 서초구 2번지", "", "Seoul", "", "06500", "")  // SKU 누락
    );

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isEqualTo(1);
    assertThat(response.getFailedRows()).hasSize(1);
    assertThat(response.getFailedRows().get(0).getRowNumber()).isEqualTo(3); // 헤더=1, 1행=2, 2행=3
  }

  /* 데이터 행이 없는 엑셀(헤더만) → successCount=0, failedRows 없음. */
  @Test
  void create_returnsZero_whenNoDataRows() throws Exception {
    StubSavePort stub = new StubSavePort();
    BulkCreateOrderService service = new BulkCreateOrderService(stub);

    MultipartFile file = buildExcel(); // 헤더만

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isZero();
    assertThat(response.getFailedRows()).isEmpty();
  }

  /* xlsx 형식 아닌 파일 → BusinessException. */
  @Test
  void create_throwsException_whenFileIsNotExcel() {
    StubSavePort stub = new StubSavePort();
    BulkCreateOrderService service = new BulkCreateOrderService(stub);

    MultipartFile file = new MockMultipartFile(
        "file", "test.txt", "text/plain", "not excel".getBytes()
    );

    assertThatThrownBy(() -> service.create(file, "SELLER-001"))
        .isInstanceOf(BusinessException.class);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /*
   * 인메모리 xlsx 파일을 생성한다.
   * 첫 번째 행은 헤더, 이후 행은 varargs 로 전달한 데이터 행으로 채운다.
   */
  private MultipartFile buildExcel(String[]... dataRows) throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet = wb.createSheet("orders");

    /* 헤더 행 */
    Row header = sheet.createRow(0);
    String[] headers = {"주문번호", "주문일시", "SKU", "수량", "상품명",
        "수령인", "연락처", "주소1", "주소2", "도시", "주/지역", "우편번호", "메모"};
    for (int i = 0; i < headers.length; i++) {
      header.createCell(i).setCellValue(headers[i]);
    }

    /* 데이터 행 */
    for (int r = 0; r < dataRows.length; r++) {
      Row row = sheet.createRow(r + 1);
      for (int c = 0; c < dataRows[r].length; c++) {
        row.createCell(c).setCellValue(dataRows[r][c]);
      }
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    wb.write(out);
    wb.close();

    return new MockMultipartFile(
        "file", "orders.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        new ByteArrayInputStream(out.toByteArray())
    );
  }

  /* 행 데이터를 가변 인수로 편리하게 생성한다. */
  private String[] row(String... cells) {
    return cells;
  }

  /*
   * 저장 호출을 기록하는 테스트용 Stub SavePort.
   */
  private static class StubSavePort implements OrderSavePort {

    final List<Order> saved = new ArrayList<>();

    @Override
    public void saveOrder(Order order) {
      saved.add(order);
    }

    @Override
    public boolean existsById(String orderNo) {
      return false;
    }
  }
}
