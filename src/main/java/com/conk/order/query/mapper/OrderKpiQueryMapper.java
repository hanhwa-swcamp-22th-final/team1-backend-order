package com.conk.order.query.mapper;

import com.conk.order.query.dto.OrderKpiQuery;
import org.apache.ibatis.annotations.Mapper;

/*
 * ORD-006 주문 KPI 집계 MyBatis Mapper 인터페이스.
 *
 * 상태별로 COUNT 쿼리를 각각 실행한다.
 * Service 에서 각 결과를 모아 OrderKpiResponse 로 조립한다.
 *
 * Service 테스트에서는 이 인터페이스를 직접 구현한 StubMapper 로 대체한다.
 */
@Mapper
public interface OrderKpiQueryMapper {

  /* 기간 내 전체 주문 건수를 조회한다. */
  int countTotal(OrderKpiQuery query);

  /* 기간 내 RECEIVED 상태 주문 건수를 조회한다. */
  int countReceived(OrderKpiQuery query);

  /* 기간 내 ALLOCATED 상태 주문 건수를 조회한다. */
  int countAllocated(OrderKpiQuery query);

  /* 기간 내 PICKING 상태 주문 건수를 조회한다. */
  int countPicking(OrderKpiQuery query);

  /* 기간 내 PACKING 상태 주문 건수를 조회한다. */
  int countPacking(OrderKpiQuery query);

  /* 기간 내 OUTBOUND_COMPLETED 상태 주문 건수를 조회한다. */
  int countOutboundCompleted(OrderKpiQuery query);

  /* 기간 내 CANCELED 상태 주문 건수를 조회한다. */
  int countCanceled(OrderKpiQuery query);
}
