package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.dto.ShipmentExportRow;
import com.conk.order.query.dto.request.WhmOrderListQuery;
import com.conk.order.query.dto.response.WhmOrderListResponse;
import com.conk.order.query.dto.response.WhmOrderSummary;
import com.conk.order.query.mapper.ShipmentExportQueryMapper;
import com.conk.order.query.mapper.WhmOrderListQueryMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * ORD-007 창고 관리자 주문 목록 조회 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * Mapper 는 StubMapper 로 대체하여 실제 DB 없이 서비스 로직만 검증한다.
 */
class WhmOrderListQueryServiceTest {

  /* warehouseId 기준으로 해당 창고 주문만 반환한다. */
  @Test
  void getWhmOrders_returnsOrdersForGivenWarehouse() {
    StubMapper stub = new StubMapper(List.of(
        summary("ORD-001"), summary("ORD-002")
    ), 2);
    WhmOrderQueryService service = new WhmOrderQueryService(stub, emptyExportMapper());

    WhmOrderListQuery query = new WhmOrderListQuery();
    query.setWarehouseId("WH-001");

    WhmOrderListResponse response = service.getWhmOrders(query);

    assertThat(response.getOrders()).hasSize(2);
    assertThat(response.getTotalCount()).isEqualTo(2);
  }

  /* 빈 결과도 정상 응답한다. */
  @Test
  void getWhmOrders_returnsEmptyList_whenNoOrders() {
    StubMapper stub = new StubMapper(List.of(), 0);
    WhmOrderQueryService service = new WhmOrderQueryService(stub, emptyExportMapper());

    WhmOrderListQuery query = new WhmOrderListQuery();
    query.setWarehouseId("WH-999");

    WhmOrderListResponse response = service.getWhmOrders(query);

    assertThat(response.getOrders()).isEmpty();
    assertThat(response.getTotalCount()).isEqualTo(0);
  }

  /* 응답에 요청한 page, size 가 그대로 담긴다. */
  @Test
  void getWhmOrders_reflectsPageAndSize() {
    StubMapper stub = new StubMapper(List.of(), 0);
    WhmOrderQueryService service = new WhmOrderQueryService(stub, emptyExportMapper());

    WhmOrderListQuery query = new WhmOrderListQuery();
    query.setWarehouseId("WH-001");
    query.setPage(1);
    query.setSize(10);

    WhmOrderListResponse response = service.getWhmOrders(query);

    assertThat(response.getPage()).isEqualTo(1);
    assertThat(response.getSize()).isEqualTo(10);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private WhmOrderSummary summary(String orderNo) {
    WhmOrderSummary s = new WhmOrderSummary();
    s.setOrderNo(orderNo);
    s.setOrderedAt(LocalDateTime.of(2026, 4, 5, 10, 0));
    s.setStatus(OrderStatus.RECEIVED);
    s.setOrderChannel(OrderChannel.MANUAL);
    s.setReceiverName("홍길동");
    s.setItemCount(1);
    return s;
  }

  private ShipmentExportQueryMapper emptyExportMapper() {
    return new ShipmentExportQueryMapper() {
      @Override
      public List<ShipmentExportRow> findShipmentExportRows() {
        return List.of();
      }
    };
  }

  /*
   * 고정 결과를 반환하는 테스트용 Stub Mapper.
   */
  private static class StubMapper implements WhmOrderListQueryMapper {

    private final List<WhmOrderSummary> orders;
    private final int total;

    StubMapper(List<WhmOrderSummary> orders, int total) {
      this.orders = orders;
      this.total = total;
    }

    @Override
    public List<WhmOrderSummary> findOrders(WhmOrderListQuery query) {
      return orders;
    }

    @Override
    public int countOrders(WhmOrderListQuery query) {
      return total;
    }
  }
}
