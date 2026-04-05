package com.conk.order.query.service;

import com.conk.order.query.dto.OrderKpiQuery;
import com.conk.order.query.dto.OrderKpiResponse;
import com.conk.order.query.mapper.OrderKpiQueryMapper;
import org.springframework.stereotype.Service;

/*
 * ORD-006 주문 KPI 집계 서비스.
 *
 * Mapper 의 상태별 COUNT 메서드를 각각 호출한 뒤
 * 결과를 OrderKpiResponse 하나로 조립해 반환한다.
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
        orderKpiQueryMapper.countPicking(query),
        orderKpiQueryMapper.countPacking(query),
        orderKpiQueryMapper.countOutboundCompleted(query),
        orderKpiQueryMapper.countCanceled(query)
    );
  }
}
