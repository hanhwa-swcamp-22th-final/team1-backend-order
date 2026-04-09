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
 *
 * 엑셀 컬럼 구조 (0-indexed):
 *   0: 주문일시  1: SKU  2: 수량  3: 상품명(선택)
 *   4: 수령인  5: 연락처  6: 주소1  7: 주소2(선택)  8: 도시
 *   9: 주/지역(선택)  10: 우편번호  11: 메모(선택)
 *
 * 주문 ID 는 OrderIdGenerator 가 채번하므로 엑셀에 포함하지 않는다.
 */
@Service
public class BulkCreateOrderService {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final OrderRepository orderRepository;
  private final OrderIdGenerator orderIdGenerator;

  public BulkCreateOrderService(OrderRepository orderRepository, OrderIdGenerator orderIdGenerator) {
    this.orderRepository = orderRepository;
    this.orderIdGenerator = orderIdGenerator;
  }

  /*
   * 엑셀 파일을 파싱해 행별로 주문을 등록한다.
   * 성공/실패 건수를 집계해 반환한다.
   *
   * Workbook 은 내부적으로 임시 파일과 메모리를 점유하므로
   * try-with-resources 로 반드시 닫아야 한다.
   *
   * @param file     xlsx 형식의 엑셀 파일
   * @param sellerId 셀러 식별자 (모든 행에 공통 적용)
   */
  public BulkCreateOrderResponse create(MultipartFile file, String sellerId) {
    int successCount = 0;
    List<FailedRow> failedRows = new ArrayList<>();

    try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = wb.getSheetAt(0);

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
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.BULK_FILE_UNREADABLE);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.BULK_FILE_FORMAT_INVALID);
    }

    return new BulkCreateOrderResponse(successCount, failedRows);
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
    String city = cell(row, 8);
    String state = cell(row, 9);
    String zipCode = cell(row, 10);
    String memo = cell(row, 11);

    LocalDateTime orderedAt = LocalDateTime.parse(orderedAtStr, DATE_TIME_FORMATTER);
    int quantity = Integer.parseInt(quantityStr);

    return Order.create(
        orderIdGenerator.generate(),
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
