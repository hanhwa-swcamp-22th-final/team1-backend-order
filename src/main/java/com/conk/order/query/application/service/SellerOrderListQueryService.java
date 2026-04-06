package com.conk.order.query.application.service;

import com.conk.order.query.application.dto.SellerOrderListQuery;
import com.conk.order.query.application.dto.SellerOrderListResponse;
import com.conk.order.query.application.dto.SellerOrderSummary;
import com.conk.order.query.infrastructure.mapper.SellerOrderListQueryMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/*
 * ORD-004 셀러 주문 목록 조회 서비스.
 *
 * Mapper 를 2번 호출한다.
 *   1. findOrders  → 현재 페이지 데이터 조회
 *   2. countOrders → 전체 건수 조회 (totalCount 용)
 * 두 결과를 합쳐 SellerOrderListResponse 로 조립해 반환한다.
 */
@Service
public class SellerOrderListQueryService {

  private final SellerOrderListQueryMapper sellerOrderListQueryMapper;

  public SellerOrderListQueryService(SellerOrderListQueryMapper sellerOrderListQueryMapper) {
    this.sellerOrderListQueryMapper = sellerOrderListQueryMapper;
  }

  /*
   * 셀러 주문 목록을 조회해 페이징 응답으로 조립한다.
   *
   * 비즈니스 로직이 거의 없고 Mapper 결과를 응답 DTO 로 감싸는 역할을 한다.
   * 정렬·필터 조건 변환이 필요해지면 이 메서드에서 처리한다.
   */
  public SellerOrderListResponse getSellerOrders(SellerOrderListQuery query) {
    /* 현재 페이지에 해당하는 주문 목록을 가져온다. */
    List<SellerOrderSummary> orders = sellerOrderListQueryMapper.findOrders(query);

    /* 페이징 없이 전체 건수를 별도로 조회한다. */
    int totalCount = sellerOrderListQueryMapper.countOrders(query);

    return new SellerOrderListResponse(orders, totalCount, query.getPage(), query.getSize());
  }
}
