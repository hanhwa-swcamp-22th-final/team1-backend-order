package com.conk.order.command.application.service;

import com.conk.order.command.application.dto.BulkValidateResponse;
import com.conk.order.command.application.dto.BulkValidateResponse.RowError;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
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
 * 엑셀 일괄 등록 사전 검증 서비스.
 *
 * DB 저장 없이 엑셀 파일의 각 행을 검증하여 오류 목록을 반환한다.
 * BulkCreateOrderService 와 동일한 컬럼 구조를 기대한다.
 */
@Service
public class BulkValidateOrderService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /*
   * 엑셀 파일의 각 행을 검증한다.
   *
   * @param file xlsx 형식의 엑셀 파일
   * @return 검증 결과 (총 행, 유효 행, 오류 목록)
   */
  public BulkValidateResponse validate(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    }

    String filename = file.getOriginalFilename();
    if (filename == null || !filename.endsWith(".xlsx")) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }

    List<RowError> errors = new ArrayList<>();
    int totalRows = 0;

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        totalRows++;
        validateRow(row, i + 1, errors);
      }
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    }

    int validRows = totalRows - errors.size();
    return new BulkValidateResponse(totalRows, validRows, errors);
  }

  /* 개별 행을 검증한다. 오류가 있으면 errors 리스트에 추가. */
  private void validateRow(Row row, int rowNum, List<RowError> errors) {
    String orderedAt = cell(row, 0);
    String sku = cell(row, 1);
    String quantity = cell(row, 2);
    String receiverName = cell(row, 4);
    String receiverPhone = cell(row, 5);
    String address1 = cell(row, 6);
    String city = cell(row, 8);
    String zipCode = cell(row, 10);

    // 필수값 검증
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
      errors.add(new RowError(rowNum, "기본주소가 비어있습니다."));
      return;
    }
    if (city.isBlank()) {
      errors.add(new RowError(rowNum, "도시가 비어있습니다."));
      return;
    }
    if (zipCode.isBlank()) {
      errors.add(new RowError(rowNum, "우편번호가 비어있습니다."));
      return;
    }

    // 형식 검증
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

  /* 셀 값을 문자열로 반환한다. null 이면 빈 문자열을 반환한다. */
  private String cell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> {
        double val = cell.getNumericCellValue();
        yield (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> "";
    };
  }
}
