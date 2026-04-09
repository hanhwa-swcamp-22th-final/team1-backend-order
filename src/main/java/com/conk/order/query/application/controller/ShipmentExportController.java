package com.conk.order.query.application.controller;

import com.conk.order.query.application.service.ShipmentExportService;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 송장 CSV 다운로드 컨트롤러.
 *
 * GET /orders/shipments/export
 * 관리자용. OUTBOUND_COMPLETED 상태의 비연동 채널 주문 송장을 CSV 로 내려준다.
 */
@RestController
@RequestMapping("/orders/shipments")
public class ShipmentExportController {

  private final ShipmentExportService shipmentExportService;

  public ShipmentExportController(ShipmentExportService shipmentExportService) {
    this.shipmentExportService = shipmentExportService;
  }

  /* 송장 CSV 를 다운로드한다. */
  @GetMapping("/export")
  public ResponseEntity<byte[]> exportCsv() {
    String csv = shipmentExportService.exportCsv();

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
