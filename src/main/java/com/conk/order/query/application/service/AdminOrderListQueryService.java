package com.conk.order.query.application.service;

import com.conk.order.query.application.dto.AdminOrderListQuery;
import com.conk.order.query.application.dto.AdminOrderListResponse;
import com.conk.order.query.application.dto.AdminOrderSummary;
import com.conk.order.query.infrastructure.mapper.AdminOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/*
 * ORD-005 관리자 주문 목록 조회 서비스.
 *
 * Mapper 를 2번 호출한다.
 *   1. findOrders  → 현재 페이지 데이터 조회
 *   2. countOrders → 전체 건수 조회 (totalCount 용)
 * 두 결과를 합쳐 AdminOrderListResponse 로 조립해 반환한다.
 */
@Service
public class AdminOrderListQueryService {

  private final AdminOrderListQueryMapper adminOrderListQueryMapper;

  public AdminOrderListQueryService(AdminOrderListQueryMapper adminOrderListQueryMapper) {
    this.adminOrderListQueryMapper = adminOrderListQueryMapper;
  }

  /* 관리자 주문 목록을 조회해 페이징 응답으로 조립한다. */
  public AdminOrderListResponse getAdminOrders(AdminOrderListQuery query) {
    List<AdminOrderSummary> orders = adminOrderListQueryMapper.findOrders(query);
    int totalCount = adminOrderListQueryMapper.countOrders(query);
    return new AdminOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }
}
