package com.conk.order.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.conk.order.command.application.dto.response.BulkCreateOrderResponse;
import com.conk.order.command.application.dto.response.BulkValidateResponse;
import com.conk.order.command.application.config.BulkUploadProperties;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/*
 * ORD-003 엑셀 일괄 주문 Command 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * OrderRepository 는 Mockito mock 으로 대체한다.
 * 엑셀 파일은 Apache POI 로 인메모리 생성해 MultipartFile 로 감싼다.
 */
@ExtendWith(MockitoExtension.class)
class BulkCreateOrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderIdGenerator orderIdGenerator;

  @Mock
  private EntityManager entityManager;

  private BulkOrderCommandService service;

  @BeforeEach
  void setUp() {
    service = new BulkOrderCommandService(
        orderRepository,
        orderIdGenerator,
        entityManager,
        bulkUploadProperties(5_000, 500)
    );
    lenient().when(orderIdGenerator.generate()).thenReturn("ORD-2026-0408-00001");
  }

  /* 유효한 2행 엑셀 → successCount=2, failedRows 없음. */
  @Test
  void create_savesAllValidRows() throws Exception {
    MultipartFile file = buildExcel(
        row("2026-04-05 10:00:00", "SKU-001", "2", "상품A", "홍길동", "010-1111-2222",
            "서울시 강남구 1번지", "", "KR", "Seoul", "06236", ""),
        row("2026-04-05 11:00:00", "SKU-002", "1", "", "김철수", "010-3333-4444",
            "서울시 서초구 2번지", "", "KR", "Seoul", "06500", "메모")
    );

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isEqualTo(2);
    assertThat(response.getFailedRows()).isEmpty();
    verify(orderRepository, times(2)).saveOrder(any());
    verify(orderRepository).flush();
    verify(entityManager).clear();
  }

  /* 1행 유효 + 1행 SKU 누락 → successCount=1, failedRows 1건. */
  @Test
  void create_collectsFailedRows_whenSomeRowsInvalid() throws Exception {
    MultipartFile file = buildExcel(
        row("2026-04-05 10:00:00", "SKU-001", "1", "", "홍길동", "010-1111-2222",
            "서울시 강남구 1번지", "", "KR", "Seoul", "06236", ""),
        row("2026-04-05 11:00:00", "",        "1", "", "김철수", "010-3333-4444",
            "서울시 서초구 2번지", "", "KR", "Seoul", "06500", "")  // SKU 누락
    );

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isEqualTo(1);
    assertThat(response.getFailedRows()).hasSize(1);
    assertThat(response.getFailedRows().get(0).getRowNumber()).isEqualTo(3); // 헤더=1, 1행=2, 2행=3
    verify(orderRepository).saveOrder(any());
    verify(orderRepository).flush();
    verify(entityManager).clear();
  }

  /* 데이터 행이 없는 엑셀(헤더만) → successCount=0, failedRows 없음. */
  @Test
  void create_returnsZero_whenNoDataRows() throws Exception {
    MultipartFile file = buildExcel(); // 헤더만

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isZero();
    assertThat(response.getFailedRows()).isEmpty();
    verify(orderRepository, never()).flush();
    verify(entityManager, never()).clear();
  }

  /* 설정한 flush interval 을 넘기면 중간 flush/clear 와 마지막 flush/clear 가 모두 호출된다. */
  @Test
  void create_flushesAndClearsPeriodically_whenRowsExceedInterval() throws Exception {
    service = new BulkOrderCommandService(
        orderRepository,
        orderIdGenerator,
        entityManager,
        bulkUploadProperties(5_000, 2)
    );
    MultipartFile file = buildExcelWithRepeatedRows(3);

    BulkCreateOrderResponse response = service.create(file, "SELLER-001");

    assertThat(response.getSuccessCount()).isEqualTo(3);
    assertThat(response.getFailedRows()).isEmpty();
    verify(orderRepository, times(3)).saveOrder(any());
    verify(orderRepository, times(2)).flush();
    verify(entityManager, times(2)).clear();
  }

  /* xlsx 형식 아닌 파일 → BusinessException. */
  @Test
  void create_throwsException_whenFileIsNotExcel() {
    MultipartFile file = new MockMultipartFile(
        "file", "test.txt", "text/plain", "not excel".getBytes()
    );

    assertThatThrownBy(() -> service.create(file, "SELLER-001"))
        .isInstanceOf(BusinessException.class);
  }

  /* 설정한 최대 행 수를 초과한 엑셀은 저장 전에 즉시 차단한다. */
  @Test
  void create_throwsException_whenRowLimitExceeded() throws Exception {
    service = new BulkOrderCommandService(
        orderRepository,
        orderIdGenerator,
        entityManager,
        bulkUploadProperties(2, 500)
    );
    MultipartFile file = buildExcelWithRepeatedRows(3);

    assertThatThrownBy(() -> service.create(file, "SELLER-001"))
        .isInstanceOf(BusinessException.class);

    verify(orderRepository, never()).saveOrder(any());
    verify(orderRepository, never()).flush();
    verify(entityManager, never()).clear();
  }

  /* 사전 검증은 총 행 수, 유효 행 수, 오류 목록을 함께 반환한다. */
  @Test
  void validate_returnsRowSummary_whenFileContainsInvalidRows() throws Exception {
    MultipartFile file = buildExcel(
        row("2026-04-05 10:00:00", "SKU-001", "1", "", "홍길동", "010-1111-2222",
            "서울시 강남구 1번지", "", "KR", "Seoul", "06236", ""),
        row("2026-04-05 11:00:00", "", "1", "", "김철수", "010-3333-4444",
            "서울시 서초구 2번지", "", "KR", "Seoul", "06500", "")
    );

    BulkValidateResponse response = service.validate(file);

    assertThat(response.getTotalRows()).isEqualTo(2);
    assertThat(response.getValidRows()).isEqualTo(1);
    assertThat(response.getErrors()).hasSize(1);
    assertThat(response.getErrors().get(0).getRow()).isEqualTo(3);
  }

  /* 사전 검증도 설정한 최대 행 수를 초과한 파일을 즉시 차단한다. */
  @Test
  void validate_throwsException_whenRowLimitExceeded() throws Exception {
    service = new BulkOrderCommandService(
        orderRepository,
        orderIdGenerator,
        entityManager,
        bulkUploadProperties(2, 500)
    );
    MultipartFile file = buildExcelWithRepeatedRows(3);

    assertThatThrownBy(() -> service.validate(file))
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
    String[] headers = {"주문일시(yyyy-MM-dd HH:mm:ss)", "SKU", "수량", "상품명",
        "수령인", "수령인 연락처", "기본 배송지", "상세 배송지", "State", "City", "Zip Code", "메모"};
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

  /* 동일한 데이터 행을 count 만큼 반복한 엑셀 파일을 만든다. */
  private MultipartFile buildExcelWithRepeatedRows(int count) throws IOException {
    Workbook wb = new XSSFWorkbook();
    Sheet sheet = wb.createSheet("orders");

    Row header = sheet.createRow(0);
    String[] headers = {"주문일시(yyyy-MM-dd HH:mm:ss)", "SKU", "수량", "상품명",
        "수령인", "수령인 연락처", "기본 배송지", "상세 배송지", "State", "City", "Zip Code", "메모"};
    for (int i = 0; i < headers.length; i++) {
      header.createCell(i).setCellValue(headers[i]);
    }

    for (int i = 0; i < count; i++) {
      Row row = sheet.createRow(i + 1);
      row.createCell(0).setCellValue("2026-04-05 10:00:00");
      row.createCell(1).setCellValue("SKU-" + i);
      row.createCell(2).setCellValue("1");
      row.createCell(3).setCellValue("상품");
      row.createCell(4).setCellValue("홍길동");
      row.createCell(5).setCellValue("010-1111-2222");
      row.createCell(6).setCellValue("서울시 강남구 1번지");
      row.createCell(7).setCellValue("");
      row.createCell(8).setCellValue("KR");
      row.createCell(9).setCellValue("Seoul");
      row.createCell(10).setCellValue("06236");
      row.createCell(11).setCellValue("");
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

  /* 테스트마다 원하는 업로드 제한값을 주입한다. */
  private BulkUploadProperties bulkUploadProperties(int maxRowLimit, int flushInterval) {
    BulkUploadProperties properties = new BulkUploadProperties();
    properties.setMaxRowLimit(maxRowLimit);
    properties.setFlushInterval(flushInterval);
    return properties;
  }
}
