package com.conk.order.query.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.conk.order.query.dto.OutboundStatsResponse;
import com.conk.order.query.mapper.OutboundStatsQueryMapper;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import org.junit.jupiter.api.Test;

/* ORD-001 출고 통계 조회 서비스의 응답 조립을 검증한다. */
public class OutboundStatsQueryServiceTest {

  /*
   * 기준 날짜 상수 (오늘 2026-04-01 = 수요일 기준으로 계산)
   * 4/6 월, 4/7 화, 4/8 수, 4/9 목, 4/11 토, 4/12 일
   */
  private static final LocalDate MON = LocalDate.of(2026, 4, 6);
  private static final LocalDate TUE = LocalDate.of(2026, 4, 7);
  private static final LocalDate WED = LocalDate.of(2026, 4, 8);
  private static final LocalDate THU = LocalDate.of(2026, 4, 9);
  private static final LocalDate SAT = LocalDate.of(2026, 4, 11);
  private static final LocalDate SUN = LocalDate.of(2026, 4, 12);

  /* 출고 대기 건수를 조회해 응답에 담는지 확인한다. */
  @Test
  void getOutboundStatsReturnsPendingOutboundCount() {
    OutboundStatsQueryService service = createService(TUE, Map.of(
        TUE, 3, TUE.minusDays(1), 2)
    );

    assertThat(service.getOutboundStats().getPendingOutboundCount()).isEqualTo(3);
  }

  /* 화~금 평일에 전날보다 증가하면 trend 는 "+N", trendType 은 "up" 이다. */
  @Test
  void getOutboundStats_weekdayIncrease_returnsTrendUp() {
    OutboundStatsQueryService service = createService(WED, Map.of(
        WED, 5,
        WED.minusDays(1), 3
    ));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isEqualTo("+2");
    assertThat(response.getTrendLabel()).isEqualTo("전날 대비");
    assertThat(response.getTrendType()).isEqualTo("up");
  }

  /* 화~금 평일에 전날보다 감소하면 trend 는 "-N", trendType 은 "down" 이다. */
  @Test
  void getOutboundStats_weekdayDecrease_returnsTrendDown() {
    OutboundStatsQueryService service = createService(THU, Map.of(
        THU, 2,
        THU.minusDays(1), 5
    ));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isEqualTo("-3");
    assertThat(response.getTrendLabel()).isEqualTo("전날 대비");
    assertThat(response.getTrendType()).isEqualTo("down");
  }

  /* 화~금 평일에 전날과 동일하면 trend 는 "0", trendType 은 "flat" 이다. */
  @Test
  void getOutboundStats_weekdayNoChange_returnsTrendFlat() {
    OutboundStatsQueryService service = createService(TUE, Map.of(
        TUE, 4,
        TUE.minusDays(1), 4
    ));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isEqualTo("0");
    assertThat(response.getTrendLabel()).isEqualTo("전날 대비");
    assertThat(response.getTrendType()).isEqualTo("flat");
  }

  /* 월요일에는 지난 금요일(-3일) 대비 추이를 계산한다. */
  @Test
  void getOutboundStats_monday_comparesToPreviousFriday() {
    LocalDate lastFriday = MON.minusDays(3);
    OutboundStatsQueryService service = createService(MON, Map.of(
        MON, 5,
        lastFriday, 2
    ));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isEqualTo("+3");
    assertThat(response.getTrendLabel()).isEqualTo("전날 대비");
    assertThat(response.getTrendType()).isEqualTo("up");
  }

  /* 토요일에는 추이 관련 필드를 null 로 반환한다. */
  @Test
  void getOutboundStats_saturday_returnsTrendNull() {
    OutboundStatsQueryService service = createService(SAT, Map.of(SAT, 3));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isNull();
    assertThat(response.getTrendLabel()).isNull();
    assertThat(response.getTrendType()).isNull();
  }

  /* 일요일에는 추이 관련 필드를 null 로 반환한다. */
  @Test
  void getOutboundStats_sunday_returnsTrendNull() {
    OutboundStatsQueryService service = createService(SUN, Map.of(SUN, 7));

    OutboundStatsResponse response = service.getOutboundStats();

    assertThat(response.getTrend()).isNull();
    assertThat(response.getTrendLabel()).isNull();
    assertThat(response.getTrendType()).isNull();
  }

  /* 토·일에도 출고 대기 건수는 정상 반환한다. */
  @Test
  void getOutboundStats_weekend_stillReturnsPendingOutboundCount() {
    OutboundStatsQueryService service = createService(SAT, Map.of(SAT, 9));

    assertThat(service.getOutboundStats().getPendingOutboundCount()).isEqualTo(9);
  }

  // ── 헬퍼 ──────────────────────────────────────────────────────────────────

  private OutboundStatsQueryService createService(
      LocalDate today, Map<LocalDate, Integer> countsByDate
  ) {
    Clock clock = Clock.fixed(
        today.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    );
    return new OutboundStatsQueryService(new StubMapper(countsByDate), clock);
  }

  /* 날짜별 건수를 고정값으로 반환하는 테스트용 Mapper */
  private static class StubMapper implements OutboundStatsQueryMapper {

    private final Map<LocalDate, Integer> countsByDate;

    StubMapper(Map<LocalDate, Integer> countsByDate) {
      this.countsByDate = countsByDate;
    }

    @Override
    public int countPendingOutboundOrdersByDate(LocalDate date) {
      return countsByDate.getOrDefault(date, 0);
    }
  }
}
