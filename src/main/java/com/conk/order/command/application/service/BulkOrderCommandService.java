package com.conk.order.command.application.service;

import com.conk.order.command.application.config.BulkUploadProperties;
import com.conk.order.command.application.dto.response.BulkCreateOrderResponse;
import com.conk.order.command.application.dto.response.BulkValidateResponse;
import com.conk.order.command.application.dto.response.BulkValidateResponse.RowError;
import com.conk.order.command.application.dto.response.FailedRow;
import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
 * ORD-003 엑셀 일괄 주문 Command 서비스.
 *
 * 엑셀 업로드의 공통 정책을 한곳에서 처리한다.
 *   - 템플릿 헤더 제공
 *   - 사전 검증
 *   - 부분 저장 방식의 일괄 등록
 *
 * 업로드 제한 행 수와 flush/clear 주기는 설정값으로 관리한다.
 */
@Service
public class BulkOrderCommandService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final String[] TEMPLATE_HEADERS = {
      "주문일시(yyyy-MM-dd HH:mm:ss)",
      "SKU",
      "수량",
      "상품명",
      "수령인",
      "수령인 연락처",
      "기본 배송지",
      "상세 배송지",
      "State",
      "City",
      "Zip Code",
      "메모"
  };

  private final OrderRepository orderRepository;
  private final OrderIdGenerator orderIdGenerator;
  private final EntityManager entityManager;
  private final BulkUploadProperties bulkUploadProperties;

  public BulkOrderCommandService(
      OrderRepository orderRepository,
      OrderIdGenerator orderIdGenerator,
      EntityManager entityManager,
      BulkUploadProperties bulkUploadProperties) {
    this.orderRepository = orderRepository;
    this.orderIdGenerator = orderIdGenerator;
    this.entityManager = entityManager;
    this.bulkUploadProperties = bulkUploadProperties;
  }

  /* 템플릿 생성에 사용할 업로드 헤더를 반환한다. */
  public String[] getTemplateHeaders() {
    return TEMPLATE_HEADERS.clone();
  }

  /* 엑셀 파일을 DB 저장 없이 검증한다. */
  public BulkValidateResponse validate(MultipartFile file) {
    validateFileBasics(file);

    List<RowError> errors = new ArrayList<>();
    int totalRows = 0;

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      validateRowLimit(sheet);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }

        totalRows++;
        validateRow(row, i + 1, errors);
      }
    } catch (BusinessException e) {
      throw e;
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }

    return new BulkValidateResponse(totalRows, totalRows - errors.size(), errors);
  }

  /*
   * 엑셀 파일을 파싱해 행별로 주문을 등록한다.
   * 성공/실패 건수를 집계해 반환한다.
   */
  public BulkCreateOrderResponse create(MultipartFile file, String sellerId) {
    validateFileBasics(file);

    int successCount = 0;
    int savedSinceLastClear = 0;
    List<FailedRow> failedRows = new ArrayList<>();

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      validateRowLimit(sheet);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }

        int rowNumber = i + 1;
        try {
          Order order = buildOrder(row, sellerId);
          orderRepository.saveOrder(order);
          successCount++;
          savedSinceLastClear++;

          if (savedSinceLastClear >= bulkUploadProperties.getFlushInterval()) {
            flushAndClear();
            savedSinceLastClear = 0;
          }
        } catch (Exception e) {
          failedRows.add(new FailedRow(rowNumber, e.getMessage()));
        }
      }

      if (savedSinceLastClear > 0) {
        flushAndClear();
      }
    } catch (BusinessException e) {
      throw e;
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }

    return new BulkCreateOrderResponse(successCount, failedRows);
  }

  /* 업로드 파일 기본 형식을 검증한다. */
  private void validateFileBasics(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    }

    String filename = file.getOriginalFilename();
    if (filename == null || !filename.endsWith(".xlsx")) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }
  }

  /* 설정한 최대 데이터 행 수를 초과하면 즉시 차단한다. */
  private void validateRowLimit(Sheet sheet) {
    if (sheet.getLastRowNum() > bulkUploadProperties.getMaxRowLimit()) {
      throw new BusinessException(ErrorCode.BULK_ROW_LIMIT_EXCEEDED);
    }
  }

  /* 영속성 컨텍스트에 남은 엔티티를 반영하고 분리한다. */
  private void flushAndClear() {
    orderRepository.flush();
    entityManager.clear();
  }

  /* 개별 행을 검증한다. 오류가 있으면 errors 리스트에 추가한다. */
  private void validateRow(Row row, int rowNum, List<RowError> errors) {
    String orderedAt = cell(row, 0);
    String sku = cell(row, 1);
    String quantity = cell(row, 2);
    String receiverName = cell(row, 4);
    String receiverPhone = cell(row, 5);
    String address1 = cell(row, 6);
    String state = cell(row, 8);
    String city = cell(row, 9);
    String zipCode = cell(row, 10);

    if (orderedAt.isBlank()) {
      errors.add(new RowError(rowNum, "주문일시가 비어있습니다."));
      return;
    }
    if (sku.isBlank()) {
      errors.add(new RowError(rowNum, "SKU가 비어있습니다."));
      return;
    }
    if (quantity.isBlank()) {
      errors.add(new RowError(rowNum, "수량이 비어있습니다."));
      return;
    }
    if (receiverName.isBlank()) {
      errors.add(new RowError(rowNum, "수령인이 비어있습니다."));
      return;
    }
    if (receiverPhone.isBlank()) {
      errors.add(new RowError(rowNum, "수령인 연락처가 비어있습니다."));
      return;
    }
    if (address1.isBlank()) {
      errors.add(new RowError(rowNum, "기본 배송지가 비어있습니다."));
      return;
    }
    if (state.isBlank()) {
      errors.add(new RowError(rowNum, "State가 비어있습니다."));
      return;
    }
    if (city.isBlank()) {
      errors.add(new RowError(rowNum, "City가 비어있습니다."));
      return;
    }
    if (zipCode.isBlank()) {
      errors.add(new RowError(rowNum, "Zip Code가 비어있습니다."));
      return;
    }

    try {
      LocalDateTime.parse(orderedAt, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      errors.add(new RowError(rowNum, "주문일시 형식이 올바르지 않습니다. (yyyy-MM-dd HH:mm:ss)"));
      return;
    }

    try {
      int qty = Integer.parseInt(quantity);
      if (qty < 1) {
        errors.add(new RowError(rowNum, "수량은 1 이상이어야 합니다."));
      }
    } catch (NumberFormatException e) {
      errors.add(new RowError(rowNum, "수량이 숫자가 아닙니다."));
    }
  }

  /* 엑셀 행을 Order 도메인 객체로 변환한다. 주문 ID 는 채번 서비스가 생성한다. */
  private Order buildOrder(Row row, String sellerId) {
    String orderedAtStr = cell(row, 0);
    String sku = cell(row, 1);
    String quantityStr = cell(row, 2);
    String productName = cell(row, 3);
    String receiverName = cell(row, 4);
    String receiverPhone = cell(row, 5);
    String address1 = cell(row, 6);
    String address2 = cell(row, 7);
    String state = cell(row, 8);
    String city = cell(row, 9);
    String zipCode = cell(row, 10);
    String memo = cell(row, 11);

    LocalDateTime orderedAt = LocalDateTime.parse(orderedAtStr, DATE_TIME_FORMATTER);
    int quantity = Integer.parseInt(quantityStr);

    return Order.create(
        orderIdGenerator.generate(),
        orderedAt,
        sellerId,
        OrderChannel.EXCEL,
        List.of(OrderItem.create(sku, quantity, productName.isBlank() ? null : productName)),
        ShippingAddress.create(
            address1,
            address2.isBlank() ? null : address2,
            city,
            state.isBlank() ? null : state,
            zipCode),
        receiverName,
        receiverPhone,
        memo.isBlank() ? null : memo
    );
  }

  /* 셀 값을 문자열로 반환한다. null 이면 빈 문자열을 반환한다. */
  private String cell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) {
      return "";
    }

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> {
        double value = cell.getNumericCellValue();
        yield value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> "";
    };
  }
}
