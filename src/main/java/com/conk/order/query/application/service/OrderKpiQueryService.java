package com.conk.order.query.application.service;

import com.conk.order.query.application.dto.OrderKpiQuery;
import com.conk.order.query.application.dto.OrderKpiResponse;
import com.conk.order.query.infrastructure.mapper.OrderKpiQueryMapper;
import org.springframework.stereotype.Service;

/*
 * ORD-006 주문 KPI 집계 서비스.
 *
 * Mapper 의 상태별 COUNT 메서드를 각각 호출한 뒤
 * 결과를 OrderKpiResponse 하나로 조립해 반환한다.
 * 호출 순서는 OrderStatus enum 흐름(RECEIVED → CANCELED)을 따른다.
 */
@Service
public class OrderKpiQueryService {

  private final OrderKpiQueryMapper orderKpiQueryMapper;

  public OrderKpiQueryService(OrderKpiQueryMapper orderKpiQueryMapper) {
    this.orderKpiQueryMapper = orderKpiQueryMapper;
  }

  /* 기간 내 주문 KPI 를 집계해 반환한다. */
  public OrderKpiResponse getKpi(OrderKpiQuery query) {
    return new OrderKpiResponse(
        orderKpiQueryMapper.countTotal(query),
        orderKpiQueryMapper.countReceived(query),
        orderKpiQueryMapper.countAllocated(query),
        orderKpiQueryMapper.countOutboundInstructed(query),
        orderKpiQueryMapper.countPickingPacking(query),
        orderKpiQueryMapper.countOutboundPending(query),
        orderKpiQueryMapper.countOutboundCompleted(query),
        orderKpiQueryMapper.countCanceled(query)
    );
  }
}
