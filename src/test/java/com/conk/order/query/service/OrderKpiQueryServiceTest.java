package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conk.order.query.dto.OrderKpiQuery;
import com.conk.order.query.dto.OrderKpiResponse;
import com.conk.order.query.mapper.OrderKpiQueryMapper;
import org.junit.jupiter.api.Test;

/*
 * ORD-006 주문 KPI 집계 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * Mapper 는 StubMapper 로 대체하여 실제 DB 없이 서비스 로직만 검증한다.
 * 검증 대상: Mapper 의 각 COUNT 결과를 OrderKpiResponse 로 올바르게 조립하는지 확인.
 */
class OrderKpiQueryServiceTest {

  /*
   * Mapper 가 반환한 각 상태별 건수가 응답 필드에 정확히 담기는지 검증한다.
   * total=10, received=3, allocated=2, outboundInstructed=1,
   * pickingPacking=1, outboundPending=1, outboundCompleted=2, canceled=1
   */
  @Test
  void getKpi_assemblesResponseFromMapperCounts() {
    StubMapper stub = new StubMapper(10, 3, 2, 1, 1, 1, 2, 1);
    OrderKpiQueryService service = new OrderKpiQueryService(stub);

    OrderKpiResponse response = service.getKpi(new OrderKpiQuery());

    assertThat(response.getTotalCount()).isEqualTo(10);
    assertThat(response.getReceivedCount()).isEqualTo(3);
    assertThat(response.getAllocatedCount()).isEqualTo(2);
    assertThat(response.getOutboundInstructedCount()).isEqualTo(1);
    assertThat(response.getPickingPackingCount()).isEqualTo(1);
    assertThat(response.getOutboundPendingCount()).isEqualTo(1);
    assertThat(response.getOutboundCompletedCount()).isEqualTo(2);
    assertThat(response.getCanceledCount()).isEqualTo(1);
  }

  /* 주문이 없으면 모든 건수가 0 으로 응답한다. */
  @Test
  void getKpi_returnsAllZero_whenNoOrders() {
    StubMapper stub = new StubMapper(0, 0, 0, 0, 0, 0, 0, 0);
    OrderKpiQueryService service = new OrderKpiQueryService(stub);

    OrderKpiResponse response = service.getKpi(new OrderKpiQuery());

    assertThat(response.getTotalCount()).isZero();
    assertThat(response.getReceivedCount()).isZero();
    assertThat(response.getCanceledCount()).isZero();
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /*
   * 고정 건수를 반환하는 테스트용 Stub Mapper.
   * 생성 시 주입한 각 상태별 건수를 그대로 반환한다.
   * 순서: total, received, allocated, outboundInstructed,
   *        pickingPacking, outboundPending, outboundCompleted, canceled
   */
  private static class StubMapper implements OrderKpiQueryMapper {

    private final int total;
    private final int received;
    private final int allocated;
    private final int outboundInstructed;
    private final int pickingPacking;
    private final int outboundPending;
    private final int outboundCompleted;
    private final int canceled;

    StubMapper(int total, int received, int allocated, int outboundInstructed,
        int pickingPacking, int outboundPending, int outboundCompleted, int canceled) {
      this.total = total;
      this.received = received;
      this.allocated = allocated;
      this.outboundInstructed = outboundInstructed;
      this.pickingPacking = pickingPacking;
      this.outboundPending = outboundPending;
      this.outboundCompleted = outboundCompleted;
      this.canceled = canceled;
    }

    @Override public int countTotal(OrderKpiQuery query)                { return total; }
    @Override public int countReceived(OrderKpiQuery query)             { return received; }
    @Override public int countAllocated(OrderKpiQuery query)            { return allocated; }
    @Override public int countOutboundInstructed(OrderKpiQuery query)   { return outboundInstructed; }
    @Override public int countPickingPacking(OrderKpiQuery query)       { return pickingPacking; }
    @Override public int countOutboundPending(OrderKpiQuery query)      { return outboundPending; }
    @Override public int countOutboundCompleted(OrderKpiQuery query)    { return outboundCompleted; }
    @Override public int countCanceled(OrderKpiQuery query)             { return canceled; }
  }
}
