package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderStatus;
import com.conk.order.query.dto.SellerOrderListQuery;
import com.conk.order.query.dto.SellerOrderListResponse;
import com.conk.order.query.dto.SellerOrderSummary;
import com.conk.order.query.mapper.SellerOrderListQueryMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * ORD-004 셀러 주문 목록 조회 서비스 단위 테스트.
 *
 * Spring 컨텍스트 없이 순수 Java 로 실행한다.
 * Mapper 는 StubMapper 로 대체하여 실제 DB 없이 서비스 로직만 검증한다.
 * 검증 대상: Mapper 결과를 응답 DTO 로 올바르게 조립하는지 확인.
 */
class SellerOrderListQueryServiceTest {

  /* sellerId 기준으로 해당 셀러 주문만 반환한다. */
  @Test
  void getSellerOrders_returnsOrdersForGivenSeller() {
    /*
     * StubMapper 에 2건을 고정값으로 세팅한다.
     * 실제 DB 쿼리 없이 "Mapper 가 N건을 주면 응답에 N건이 담기는가"를 검증한다.
     */
    StubMapper stub = new StubMapper(List.of(
        summary("ORD-001"), summary("ORD-002")
    ), 2);
    SellerOrderListQueryService service = new SellerOrderListQueryService(stub);

    SellerOrderListQuery query = new SellerOrderListQuery();
    query.setSellerId("SELLER-001");

    SellerOrderListResponse response = service.getSellerOrders(query);

    assertThat(response.getOrders()).hasSize(2);
    assertThat(response.getTotalCount()).isEqualTo(2);
  }

  /* 빈 결과도 정상 응답한다. */
  @Test
  void getSellerOrders_returnsEmptyList_whenNoOrders() {
    StubMapper stub = new StubMapper(List.of(), 0);
    SellerOrderListQueryService service = new SellerOrderListQueryService(stub);

    SellerOrderListQuery query = new SellerOrderListQuery();
    query.setSellerId("SELLER-999");

    SellerOrderListResponse response = service.getSellerOrders(query);

    assertThat(response.getOrders()).isEmpty();
    assertThat(response.getTotalCount()).isEqualTo(0);
  }

  /* 응답에 요청한 page, size 가 그대로 담긴다. */
  @Test
  void getSellerOrders_reflectsPageAndSize() {
    /*
     * 서비스가 query.getPage(), query.getSize() 를 응답에 그대로 넣는지 확인한다.
     * 프론트에서 "현재 2페이지, 10개씩" 표시에 필요한 값이다.
     */
    StubMapper stub = new StubMapper(List.of(), 0);
    SellerOrderListQueryService service = new SellerOrderListQueryService(stub);

    SellerOrderListQuery query = new SellerOrderListQuery();
    query.setSellerId("SELLER-001");
    query.setPage(2);
    query.setSize(10);

    SellerOrderListResponse response = service.getSellerOrders(query);

    assertThat(response.getPage()).isEqualTo(2);
    assertThat(response.getSize()).isEqualTo(10);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /* 테스트용 주문 요약 객체를 생성한다. */
  private SellerOrderSummary summary(String orderNo) {
    SellerOrderSummary s = new SellerOrderSummary();
    s.setOrderNo(orderNo);
    s.setOrderedAt(LocalDateTime.of(2026, 4, 3, 10, 0));
    s.setStatus(OrderStatus.RECEIVED);
    s.setOrderChannel(OrderChannel.MANUAL);
    s.setReceiverName("홍길동");
    s.setItemCount(1);
    return s;
  }

  /*
   * 고정 결과를 반환하는 테스트용 Stub Mapper.
   *
   * SellerOrderListQueryMapper 인터페이스를 직접 구현해
   * 생성 시 주입한 orders/total 을 그대로 반환한다.
   * 실제 SQL 실행 없이 서비스 로직만 격리해서 테스트할 수 있다.
   */
  private static class StubMapper implements SellerOrderListQueryMapper {

    private final List<SellerOrderSummary> orders;
    private final int total;

    StubMapper(List<SellerOrderSummary> orders, int total) {
      this.orders = orders;
      this.total = total;
    }

    @Override
    public List<SellerOrderSummary> findOrders(SellerOrderListQuery query) {
      return orders; /* 항상 고정된 리스트를 반환한다. */
    }

    @Override
    public int countOrders(SellerOrderListQuery query) {
      return total; /* 항상 고정된 건수를 반환한다. */
    }
  }
}
