package com.conk.order.query.application.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.ShippingAddress;
import com.conk.order.command.domain.repository.OrderRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * ORD-001 통합 테스트.
 *
 * @WebMvcTest 는 서비스를 Mock 으로 대체하지만,
 * 이 테스트는 전체 스택(Controller → Service → MyBatis Mapper → H2 DB)을 실제로 실행한다.
 * 즉, 실제 SQL 이 올바르게 실행되는지까지 검증한다.
 */
@SpringBootTest         // 전체 Spring 컨텍스트를 로드한다. (Controller, Service, Mapper, DB 전부 실제 동작)
@AutoConfigureMockMvc   // @SpringBootTest 환경에서 MockMvc 를 쓸 수 있게 자동 설정한다.
@Transactional          // 각 테스트가 끝나면 DB 변경사항을 자동으로 롤백한다. (테스트 간 데이터 격리)
class OutboundStatsIntegrationTest {

  /*
   * HTTP 요청을 실제 서버 없이 시뮬레이션하는 객체.
   * @WebMvcTest 에서도 썼던 것과 동일하게 사용한다.
   */
  @Autowired
  private MockMvc mockMvc;

  /*
   * 테스트 데이터를 DB 에 삽입하기 위해 주입받는다.
   * Service 나 Mapper 를 거치지 않고 직접 DB 에 넣는다.
   */
  @Autowired
  private OrderRepository orderRepository;

  /*
   * 오늘 날짜를 가져오기 위해 주입받는다.
   * LocalDate.now() 를 직접 쓰지 않고 Clock 을 쓰는 이유:
   * 서비스도 같은 Clock 을 쓰기 때문에 날짜가 항상 일치한다.
   */
  @Autowired
  private Clock clock;

  /*
   * 오늘 날짜 RECEIVED 주문 2건을 DB 에 삽입했을 때
   * pendingOutboundCount 가 2 로 반환되는지 검증한다.
   */
  @Test
  void getOutboundStats_returnsActualCountFromDb() throws Exception {
    // given — H2 DB 에 오늘 날짜 RECEIVED 주문 2건을 직접 삽입한다.
    LocalDate today = LocalDate.now(clock);
    orderRepository.save(createOrder("ORD-001", today));
    orderRepository.save(createOrder("ORD-002", today));

    /*
     * when & then
     * 실제 HTTP 요청 → Controller → Service → MyBatis Mapper → H2 조회 → 응답
     * Mock 없이 전체 흐름이 실행된다.
     */
    mockMvc.perform(get("/orders/outbound/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.pendingOutboundCount").value(2));
  }

  /*
   * 오늘 날짜 주문이 없을 때 pendingOutboundCount 가 0 으로 반환되는지 검증한다.
   * DB 에 아무것도 삽입하지 않고 요청을 보낸다.
   */
  @Test
  void getOutboundStats_returnsZeroWhenNoOrders() throws Exception {
    mockMvc.perform(get("/orders/outbound/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.pendingOutboundCount").value(0));
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  /*
   * 테스트용 Order 를 생성하는 헬퍼 메서드.
   * orderedAt 에 date 를 넣어 MyBatis SQL 의 DATE(ordered_at) = #{date} 조건에 맞춘다.
   */
  private Order createOrder(String orderNo, LocalDate date) {
    return Order.create(
        orderNo,
        date.atStartOfDay(),    // LocalDate → LocalDateTime 변환 (2026-04-03 → 2026-04-03T00:00:00)
        "SELLER-001",
        OrderChannel.MANUAL,
        List.of(OrderItem.create("SKU-001", 1, null)),
        ShippingAddress.create("서울시 강남구 테헤란로 123", null, "Seoul", null, "06236"),
        "홍길동",
        "010-1234-5678",
        null
    );
  }
}
