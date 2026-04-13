package com.conk.order.query.controller;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.common.dto.ApiResponse;
import com.conk.order.query.dto.request.WhmOrderListQuery;
import com.conk.order.query.dto.response.WhmOrderListResponse;
import com.conk.order.query.service.WhmOrderQueryService;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 창고 관리자(WHM) 주문 조회 컨트롤러.
 *
 *   - GET /orders/whm                : 창고 관리자 주문 목록 (ORD-007)
 *   - GET /orders/shipments/export   : 송장 CSV 다운로드 (OUTBOUND_COMPLETED · 비연동 채널)
 *
 * WHM Actor 가 자신이 담당하는 창고의 주문 운영·출고 확인 용도로 사용하는 엔드포인트를 묶는다.
 */
@RestController
@RequestMapping("/orders")
public class WhmOrderQueryController {

  private final WhmOrderQueryService whmOrderQueryService;

  public WhmOrderQueryController(WhmOrderQueryService whmOrderQueryService) {
    this.whmOrderQueryService = whmOrderQueryService;
  }

  /*
   * GET /orders/whm — 창고 관리자 주문 목록을 조회한다.
   *
   * warehouseId 는 필수. WHM 은 자신이 담당하는 창고의 주문만 조회할 수 있다.
   */
  @GetMapping("/whm")
  public ResponseEntity<ApiResponse<WhmOrderListResponse>> getWhmOrders(
      @RequestParam String warehouseId,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    WhmOrderListQuery query = new WhmOrderListQuery();
    query.setWarehouseId(warehouseId);
    query.setStatus(status);
    query.setStartDate(startDate);
    query.setEndDate(endDate);
    query.setPage(page);
    query.setSize(size);

    return ResponseEntity.ok(ApiResponse.success(whmOrderQueryService.getWhmOrders(query)));
  }

  /* GET /orders/shipments/export — 송장 CSV 를 다운로드한다. */
  @GetMapping("/shipments/export")
  public ResponseEntity<byte[]> exportShipmentCsv() {
    String csv = whmOrderQueryService.exportCsv();

    // UTF-8 BOM 추가 (엑셀에서 한글 깨짐 방지)
    byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
    byte[] body = new byte[bom.length + csvBytes.length];
    System.arraycopy(bom, 0, body, 0, bom.length);
    System.arraycopy(csvBytes, 0, body, bom.length, csvBytes.length);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=shipment_export.csv")
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .body(body);
  }
}
