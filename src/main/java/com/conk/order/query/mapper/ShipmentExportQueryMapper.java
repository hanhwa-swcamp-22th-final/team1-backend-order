package com.conk.order.query.mapper;

import com.conk.order.query.dto.ShipmentExportRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/*
 * 송장 CSV 다운로드용 MyBatis Mapper.
 *
 * OUTBOUND_COMPLETED 상태의 비연동 채널(MANUAL, EXCEL) 주문을 조회한다.
 */
@Mapper
public interface ShipmentExportQueryMapper {

  /* 출고 완료된 비연동 채널 주문의 송장 정보를 조회한다. */
  List<ShipmentExportRow> findShipmentExportRows();
}
