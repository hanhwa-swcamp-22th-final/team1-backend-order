package com.conk.order.query.service;

import com.conk.order.query.dto.request.OrderKpiQuery;
import com.conk.order.query.dto.response.OrderKpiResponse;
import com.conk.order.query.dto.response.OutboundStatsResponse;
import com.conk.order.query.mapper.OrderKpiQueryMapper;
import com.conk.order.query.mapper.OutboundStatsQueryMapper;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/*
 * 주문 대시보드 집계 조회 서비스.
 *
 * 관리자 대시보드 화면에서 사용하는 집계·통계 조회를 한곳에 묶는다.
 *   - 출고 통계
 *   - 주문 KPI 집계
 */
@Service
public class OrderDashboardQueryService {

  private final OutboundStatsQueryMapper outboundStatsQueryMapper;
  private final OrderKpiQueryMapper orderKpiQueryMapper;
  private final Clock clock;

  public OrderDashboardQueryService(
      OutboundStatsQueryMapper outboundStatsQueryMapper,
      OrderKpiQueryMapper orderKpiQueryMapper,
      Clock clock) {
    this.outboundStatsQueryMapper = outboundStatsQueryMapper;
    this.orderKpiQueryMapper = orderKpiQueryMapper;
    this.clock = clock;
  }

  /* 출고 통계를 조회한다. */
  public OutboundStatsResponse getOutboundStats() {
    LocalDate today = LocalDate.now(clock);
    int todayCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(today);

    DayOfWeek dayOfWeek = today.getDayOfWeek();
    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
      return OutboundStatsResponse.builder()
          .pendingOutboundCount(todayCount)
          .build();
    }

    LocalDate prevWorkday = dayOfWeek == DayOfWeek.MONDAY
        ? today.minusDays(3)
        : today.minusDays(1);

    int prevCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(prevWorkday);
    int delta = todayCount - prevCount;

    String trend = delta > 0 ? "+" + delta : String.valueOf(delta);
    String trendType = delta > 0 ? "up" : delta < 0 ? "down" : "flat";

    return OutboundStatsResponse.builder()
        .pendingOutboundCount(todayCount)
        .trend(trend)
        .trendLabel("전 영업일 대비")
        .trendType(trendType)
        .build();
  }

  /* 기간 내 주문 KPI 를 집계해 반환한다. */
  public OrderKpiResponse getKpi(OrderKpiQuery query) {
    return new OrderKpiResponse(
        orderKpiQueryMapper.countTotal(query),
        orderKpiQueryMapper.countReceived(query),
        orderKpiQueryMapper.countAllocated(query),
        orderKpiQueryMapper.countOutboundInstructed(query),
        orderKpiQueryMapper.countPicking(query),
        orderKpiQueryMapper.countPacking(query),
        orderKpiQueryMapper.countOutboundPending(query),
        orderKpiQueryMapper.countOutboundCompleted(query),
        orderKpiQueryMapper.countCanceled(query)
    );
  }
}
