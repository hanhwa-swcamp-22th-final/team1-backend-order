package com.conk.order.query.service;

import com.conk.order.query.dto.ShipmentExportRow;
import com.conk.order.query.dto.request.WhmOrderListQuery;
import com.conk.order.query.dto.response.WhmOrderListResponse;
import com.conk.order.query.dto.response.WhmOrderSummary;
import com.conk.order.query.mapper.ShipmentExportQueryMapper;
import com.conk.order.query.mapper.WhmOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 창고 관리자 주문 조회 서비스.
 *
 * 창고 관리자 Actor 가 사용하는 조회를 한곳에 묶는다.
 *   - 창고 주문 목록 조회
 *   - 송장 CSV 다운로드
 */
@Service
public class WhmOrderQueryService {

  private static final String CSV_HEADER =
      "주문번호,송장번호,채널,수령인,연락처,기본주소,상세주소,도시,주/지역,우편번호,국가,출고일시";

  private final WhmOrderListQueryMapper whmOrderListQueryMapper;
  private final ShipmentExportQueryMapper shipmentExportQueryMapper;

  public WhmOrderQueryService(
      WhmOrderListQueryMapper whmOrderListQueryMapper,
      ShipmentExportQueryMapper shipmentExportQueryMapper) {
    this.whmOrderListQueryMapper = whmOrderListQueryMapper;
    this.shipmentExportQueryMapper = shipmentExportQueryMapper;
  }

  /* 창고 관리자 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public WhmOrderListResponse getWhmOrders(WhmOrderListQuery query) {
    List<WhmOrderSummary> orders = whmOrderListQueryMapper.findOrders(query);
    int totalCount = whmOrderListQueryMapper.countOrders(query);
    return new WhmOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }

  /* 출고 완료된 비연동 채널 주문을 CSV 문자열로 반환한다. */
  @Transactional(readOnly = true)
  public String exportCsv() {
    List<ShipmentExportRow> rows = shipmentExportQueryMapper.findShipmentExportRows();

    StringBuilder sb = new StringBuilder();
    sb.append(CSV_HEADER).append("\n");

    for (ShipmentExportRow row : rows) {
      sb.append(escape(row.getOrderId())).append(",");
      sb.append(escape(row.getTrackingCode())).append(",");
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
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
