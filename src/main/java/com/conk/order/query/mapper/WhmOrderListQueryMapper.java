package com.conk.order.query.mapper;

import com.conk.order.query.dto.WhmOrderListQuery;
import com.conk.order.query.dto.WhmOrderSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/*
 * ORD-007 창고 관리자 주문 목록 조회 MyBatis Mapper 인터페이스.
 *
 * 실제 SQL 은 WhmOrderListQueryMapper.xml 에 정의한다.
 * Service 테스트에서는 이 인터페이스를 직접 구현한 StubMapper 로 대체한다.
 */
@Mapper
public interface WhmOrderListQueryMapper {

  /* 현재 페이지에 해당하는 주문 목록을 조회한다. */
  List<WhmOrderSummary> findOrders(WhmOrderListQuery query);

  /* 필터 조건에 해당하는 전체 주문 건수를 조회한다. */
  int countOrders(WhmOrderListQuery query);
}
