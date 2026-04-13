package com.conk.order.query.mapper;

import com.conk.order.query.dto.request.AdminOrderListQuery;
import com.conk.order.query.dto.response.AdminOrderSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/*
 * ORD-005 관리자 주문 목록 조회 MyBatis Mapper 인터페이스.
 *
 * 실제 SQL 은 AdminOrderListQueryMapper.xml 에 정의한다.
 * @Mapper: MyBatis 가 이 인터페이스의 구현체를 자동으로 생성해 Spring 빈으로 등록한다.
 *
 * Service 테스트에서는 이 인터페이스를 직접 구현한 StubMapper 로 대체한다.
 * 덕분에 Spring 컨텍스트와 실제 DB 없이 Service 로직만 격리해서 테스트할 수 있다.
 */
@Mapper
public interface AdminOrderListQueryMapper {

  /*
   * 현재 페이지에 해당하는 주문 목록을 조회한다.
   * XML: LIMIT #{size} OFFSET #{offset} 으로 페이징 처리.
   */
  List<AdminOrderSummary> findOrders(AdminOrderListQuery query);

  /*
   * 필터 조건에 해당하는 전체 주문 건수를 조회한다.
   * 페이징 없이 COUNT(*) 만 실행한다.
   */
  int countOrders(AdminOrderListQuery query);
}
