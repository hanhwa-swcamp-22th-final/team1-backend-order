package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.dto.AdminOrderStatus;
import com.conk.order.query.dto.request.AdminOrderListQuery;
import com.conk.order.query.dto.response.AdminOrderListResponse;
import com.conk.order.query.dto.response.AdminOrderSummary;
import com.conk.order.query.mapper.AdminOrderListQueryMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * ORD-005 관리자 주문 목록 조회 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * Mapper 는 StubMapper 로 대체하여 실제 DB 없이 서비스 로직만 검증한다.
 * 검증 대상: Mapper 결과를 응답 DTO 로 올바르게 조립하는지 확인.
 */
class AdminOrderListQueryServiceTest {

  /* 필터 없이 조회하면 Mapper 가 반환한 전체 주문 목록이 응답에 담긴다. */
  @Test
  void getAdminOrders_returnsAllOrders() {
    /*
     * StubMapper 에 2건을 고정값으로 세팅한다.
     * SELLER-001, SELLER-002 각 1건씩 — 관리자는 모든 셀러 주문을 볼 수 있다.
     */
    StubMapper stub = new StubMapper(List.of(
        summary("ORD-001", "SELLER-001"),
        summary("ORD-002", "SELLER-002")
    ), 2);
    AdminOrderQueryService service = new AdminOrderQueryService(stub, null);

    AdminOrderListQuery query = new AdminOrderListQuery();

    AdminOrderListResponse response = service.getAdminOrders(query);

    assertThat(response.getOrders()).hasSize(2);
    assertThat(response.getTotalCount()).isEqualTo(2);
    assertThat(response.getOrders().get(0).getStatus()).isEqualTo(AdminOrderStatus.PENDING);
    assertThat(response.getOrders().get(0).getCompany()).isEqualTo("SELLER-001");
  }

  /* 빈 결과도 정상 응답한다. */
  @Test
  void getAdminOrders_returnsEmptyList_whenNoOrders() {
    StubMapper stub = new StubMapper(List.of(), 0);
    AdminOrderQueryService service = new AdminOrderQueryService(stub, null);

    AdminOrderListResponse response = service.getAdminOrders(new AdminOrderListQuery());

    assertThat(response.getOrders()).isEmpty();
    assertThat(response.getTotalCount()).isEqualTo(0);
  }

  /* 응답에 요청한 page, size 가 그대로 담긴다. */
  @Test
  void getAdminOrders_reflectsPageAndSize() {
    StubMapper stub = new StubMapper(List.of(), 0);
    AdminOrderQueryService service = new AdminOrderQueryService(stub, null);

    AdminOrderListQuery query = new AdminOrderListQuery();
    query.setPage(2);
    query.setSize(10);

    AdminOrderListResponse response = service.getAdminOrders(query);

    assertThat(response.getPage()).isEqualTo(2);
    assertThat(response.getSize()).isEqualTo(10);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /* 테스트용 주문 요약 객체를 생성한다. */
  private AdminOrderSummary summary(String orderId, String sellerId) {
    AdminOrderSummary s = new AdminOrderSummary();
    s.setId(orderId);
    s.setOrderedAt(LocalDateTime.of(2026, 4, 5, 10, 0));
    s.setRawStatus(OrderStatus.RECEIVED);
    s.setChannel("MANUAL");
    s.setCompany(sellerId);
    s.setWarehouse("WH-001");
    s.setSkuCount(1);
    s.setQty(1);
    s.setDestState("Seoul");
    return s;
  }

  /*
   * 고정 결과를 반환하는 테스트용 Stub Mapper.
   *
   * AdminOrderListQueryMapper 인터페이스를 직접 구현해
   * 생성 시 주입한 orders/total 을 그대로 반환한다.
   */
  private static class StubMapper implements AdminOrderListQueryMapper {

    private final List<AdminOrderSummary> orders;
    private final int total;

    StubMapper(List<AdminOrderSummary> orders, int total) {
      this.orders = orders;
      this.total = total;

    }

    @Override
    public List<AdminOrderSummary> findOrders(AdminOrderListQuery query) {
      return orders;
    }

    @Override
    public int countOrders(AdminOrderListQuery query) {
      return total;
    }
  }
}
