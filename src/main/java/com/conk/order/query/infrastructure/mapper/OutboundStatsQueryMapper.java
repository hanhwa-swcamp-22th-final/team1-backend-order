package com.conk.order.query.infrastructure.mapper;

import java.time.LocalDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboundStatsQueryMapper {

  /* 특정 날짜의 출고 대기 건수를 조회한다. */
  int countPendingOutboundOrdersByDate(@Param("date") LocalDate date);
}
