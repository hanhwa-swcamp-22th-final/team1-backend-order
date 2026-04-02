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

  // 1. 오늘 날짜를 Clock에서 가져온다 (테스트에서 날짜를 고정할 수 있게 Clock을 주입받음)
  public OutboundStatsQueryService(OutboundStatsQueryMapper outboundStatsQueryMapper, Clock clock) {
    this.outboundStatsQueryMapper = outboundStatsQueryMapper;
    this.clock = clock; // 현재 시간을 직접 하드코딩해서 쓰지 않도록 시간을 주입받는 객체
  }
  // 2. 오늘 RECEIVED 건수를 DB에서 조회한다
  public OutboundStatsResponse getOutboundStats() {
    LocalDate today = LocalDate.now(clock);
    int todayCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(today);

//     3. 토·일은 추이 계산 대상이 아니므로 건수만 반환한다.
    DayOfWeek dow = today.getDayOfWeek();
    if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
      return OutboundStatsResponse.builder()
          .pendingOutboundCount(todayCount)
          .build();
    }

    /* 4. 전 영업일 계산 : 월요일은 지난 금요일(-3일), 화~금은 어제(-1일)와 비교한다. */
    LocalDate prevWorkday = dow == DayOfWeek.MONDAY
        ? today.minusDays(3) // 3항 연산자 ( ? A : B -> 조건이 참이면 A, 거짓이면 B )
        : today.minusDays(1);

    // 5. 비교 기준일 건수를 DB에서 조회한다
    int prevCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(prevWorkday);
    // 6. 증감 계산
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