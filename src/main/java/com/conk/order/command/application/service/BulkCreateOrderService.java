package com.conk.order.command.application.service;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.application.dto.BulkCreateOrderResponse;
import com.conk.order.command.application.dto.FailedRow;
import com.conk.order.command.domain.repository.OrderRepository;
import com.conk.order.common.exception.BusinessException;
import com.conk.order.common.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/*
 * ORD-003 엑셀 일괄 주문 등록 서비스.
 *
 * 엑셀 파일을 파싱해 행별로 주문을 저장한다.
 * 실패한 행은 건너뛰고 성공한 행만 저장하는 정책(부분 저장)을 따른다.
 * 각 행은 독립 트랜잭션으로 처리되므로 이 클래스에는 @Transactional 을 붙이지 않는다.
 * OrderSavePort.saveOrder() 는 CreateOrderService 에서 @Transactional 이 보장된다.
 *
 * 엑셀 컬럼 구조 (0-indexed):
 *   0: 주문번호(선택)  1: 주문일시  2: SKU  3: 수량  4: 상품명(선택)
 *   5: 수령인  6: 연락처  7: 주소1  8: 주소2(선택)  9: 도시
 *   10: 주/지역(선택)  11: 우편번호  12: 메모(선택)
 */
@Service
public class BulkCreateOrderService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final OrderRepository orderRepository;

  public BulkCreateOrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  /*
   * 엑셀 파일을 파싱해 행별로 주문을 등록한다.
   * 성공/실패 건수를 집계해 반환한다.
   *
   * @param file     xlsx 형식의 엑셀 파일
   * @param sellerId 셀러 식별자 (모든 행에 공통 적용)
   */
  public BulkCreateOrderResponse create(MultipartFile file, String sellerId) {
    Sheet sheet = parseSheet(file);

    int successCount = 0;
    List<FailedRow> failedRows = new ArrayList<>();

    /* 첫 번째 행(인덱스 0)은 헤더이므로 1부터 시작한다. */
    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);
      if (row == null) continue;

      int rowNumber = i + 1; // 헤더=1행, 데이터 첫 행=2행 기준
      try {
        Order order = buildOrder(row, sellerId);
        orderRepository.saveOrder(order);
        successCount++;
      } catch (Exception e) {
        failedRows.add(new FailedRow(rowNumber, e.getMessage()));
      }
    }

    return new BulkCreateOrderResponse(successCount, failedRows);
  }

  /*
   * MultipartFile 을 xlsx Sheet 로 변환한다.
   * xlsx 형식이 아니거나 파싱 실패 시 IllegalArgumentException 을 던진다.
   */
  private Sheet parseSheet(MultipartFile file) {
    try {
      Workbook wb = new XSSFWorkbook(file.getInputStream());
      return wb.getSheetAt(0);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }
  }

  /* 엑셀 행을 Order 도메인 객체로 변환한다. */
  private Order buildOrder(Row row, String sellerId) {
    String orderNo = cell(row, 0);
    String orderedAtStr = cell(row, 1);
    String sku = cell(row, 2);
    String quantityStr = cell(row, 3);
    String productName = cell(row, 4);
    String receiverName = cell(row, 5);
    String receiverPhone = cell(row, 6);
    String address1 = cell(row, 7);
    String address2 = cell(row, 8);
    String city = cell(row, 9);
    String state = cell(row, 10);
    String zipCode = cell(row, 11);
    String memo = cell(row, 12);

    /* 주문번호 — 없으면 UUID 자동 생성. */
    String resolvedOrderNo = (orderNo.isBlank()) ? UUID.randomUUID().toString() : orderNo;

    LocalDateTime orderedAt = LocalDateTime.parse(orderedAtStr, DATE_TIME_FORMATTER);
    int quantity = Integer.parseInt(quantityStr);

    return Order.create(
        resolvedOrderNo,
        orderedAt,
        sellerId,
        OrderChannel.MANUAL,
        List.of(OrderItem.create(sku, quantity, productName.isBlank() ? null : productName)),
        ShippingAddress.create(address1, address2.isBlank() ? null : address2,
            city, state.isBlank() ? null : state, zipCode),
        receiverName,
        receiverPhone,
        memo.isBlank() ? null : memo
    );
  }

  /* 셀 값을 문자열로 반환한다. null 이면 빈 문자열을 반환한다. */
  private String cell(Row row, int index) {
    Cell cell = row.getCell(index);
    if (cell == null) return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> {
        double val = cell.getNumericCellValue();
        /* 정수형 숫자(수량 등)는 소수점 없이 반환한다. */
        yield (val == Math.floor(val)) ? String.valueOf((long) val) : String.valueOf(val);
      }
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      default -> "";
    };
  }
}
