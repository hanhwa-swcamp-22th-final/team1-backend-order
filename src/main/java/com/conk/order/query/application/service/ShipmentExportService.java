package com.conk.order.query.application.service;

import com.conk.order.query.application.dto.ShipmentExportRow;
import com.conk.order.query.infrastructure.mapper.ShipmentExportQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 송장 CSV 다운로드 서비스.
 *
 * OUTBOUND_COMPLETED 상태의 비연동 채널 주문을 조회하고
 * CSV 문자열로 변환한다.
 */
@Service
public class ShipmentExportService {

  private static final String CSV_HEADER =
      "주문번호,송장번호,채널,수령인,연락처,기본주소,상세주소,도시,주/지역,우편번호,국가,출고일시";

  private final ShipmentExportQueryMapper shipmentExportQueryMapper;

  public ShipmentExportService(ShipmentExportQueryMapper shipmentExportQueryMapper) {
    this.shipmentExportQueryMapper = shipmentExportQueryMapper;
  }

  /* 출고 완료된 비연동 채널 주문을 CSV 문자열로 반환한다. */
  @Transactional(readOnly = true)
  public String exportCsv() {
    List<ShipmentExportRow> rows = shipmentExportQueryMapper.findShipmentExportRows();

    StringBuilder sb = new StringBuilder();
    sb.append(CSV_HEADER).append("\n");

    for (ShipmentExportRow row : rows) {
      sb.append(escape(row.getOrderId())).append(",");
      sb.append(escape(row.getInvoiceNo())).append(",");
      sb.append(escape(row.getOrderChannel())).append(",");
      sb.append(escape(row.getReceiverName())).append(",");
      sb.append(escape(row.getReceiverPhoneNo())).append(",");
      sb.append(escape(row.getAddress1())).append(",");
      sb.append(escape(row.getAddress2())).append(",");
      sb.append(escape(row.getCity())).append(",");
      sb.append(escape(row.getState())).append(",");
      sb.append(escape(row.getZipCode())).append(",");
      sb.append(escape(row.getCountry())).append(",");
      sb.append(escape(row.getShippedAt())).append("\n");
    }

    return sb.toString();
  }

  /* CSV 특수문자(쉼표, 따옴표, 줄바꿈) 를 이스케이프한다. null 은 빈 문자열로 처리. */
  private String escape(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
