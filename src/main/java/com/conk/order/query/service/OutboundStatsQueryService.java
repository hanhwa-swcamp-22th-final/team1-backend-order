package com.conk.order.query.service;

import com.conk.order.query.dto.OutboundStatsResponse;
import com.conk.order.query.mapper.OutboundStatsQueryMapper;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/* ORD-001 출고 통계 조회 서비스를 담당한다. */
@Service
public class OutboundStatsQueryService {

  private final OutboundStatsQueryMapper outboundStatsQueryMapper;
  private final Clock clock;

  public OutboundStatsQueryService(OutboundStatsQueryMapper outboundStatsQueryMapper, Clock clock) {
    this.outboundStatsQueryMapper = outboundStatsQueryMapper;
    this.clock = clock;
  }

  public OutboundStatsResponse getOutboundStats() {
    LocalDate today = LocalDate.now(clock);
    int todayCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(today);

    /* 토·일은 추이 계산 대상이 아니므로 건수만 반환한다. */
    DayOfWeek dow = today.getDayOfWeek();
    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
      return OutboundStatsResponse.builder()
          .pendingOutboundCount(todayCount)
          .build();
    }

    /* 월요일은 지난 금요일(-3일), 화~금은 어제(-1일)와 비교한다. */
    LocalDate prevWorkday = dow == DayOfWeek.MONDAY
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
}