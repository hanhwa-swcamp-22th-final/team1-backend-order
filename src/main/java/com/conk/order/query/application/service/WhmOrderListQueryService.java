package com.conk.order.query.application.service;

import com.conk.order.query.application.dto.WhmOrderListQuery;
import com.conk.order.query.application.dto.WhmOrderListResponse;
import com.conk.order.query.application.dto.WhmOrderSummary;
import com.conk.order.query.infrastructure.mapper.WhmOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/*
 * ORD-007 창고 관리자 주문 목록 조회 서비스.
 *
 * Mapper 를 2번 호출한다.
 *   1. findOrders  → 현재 페이지 데이터 조회
 *   2. countOrders → 전체 건수 조회 (totalCount 용)
 * 두 결과를 합쳐 WhmOrderListResponse 로 조립해 반환한다.
 */
@Service
public class WhmOrderListQueryService {

  private final WhmOrderListQueryMapper whmOrderListQueryMapper;

  public WhmOrderListQueryService(WhmOrderListQueryMapper whmOrderListQueryMapper) {
    this.whmOrderListQueryMapper = whmOrderListQueryMapper;
  }

  /* 창고 관리자 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public WhmOrderListResponse getWhmOrders(WhmOrderListQuery query) {
    List<WhmOrderSummary> orders = whmOrderListQueryMapper.findOrders(query);
    int totalCount = whmOrderListQueryMapper.countOrders(query);
    return new WhmOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }
}
