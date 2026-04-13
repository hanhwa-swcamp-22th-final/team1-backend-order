package com.conk.order.query.mapper;

import com.conk.order.query.dto.request.SellerOrderListQuery;
import com.conk.order.query.dto.response.SellerOrderSummary;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/*
 * ORD-004 셀러 주문 목록 조회 MyBatis Mapper 인터페이스.
 *
 * @Mapper: Spring 이 이 인터페이스의 구현체를 자동으로 생성해 빈으로 등록한다.
 * SQL 은 src/main/resources/mappers/SellerOrderListQueryMapper.xml 에 작성한다.
 * 메서드명이 XML 의 <select id="..."> 와 일치해야 한다.
 */
@Mapper
public interface SellerOrderListQueryMapper {

  /*
   * 셀러 주문 목록을 필터·페이징 조건으로 조회한다.
   *
   * XML 에서 query 객체의 필드를 #{sellerId}, #{status} 처럼 바인딩한다.
   * status/startDate/endDate 는 null 이면 <if> 조건으로 필터를 건너뛴다.
   * LIMIT #{size} OFFSET #{offset} 으로 페이징 처리한다.
   */
  List<SellerOrderSummary> findOrders(SellerOrderListQuery query);

  /*
   * 필터 조건에 맞는 전체 주문 수를 반환한다.
   *
   * findOrders 와 동일한 WHERE 조건을 사용하되 페이징 없이 COUNT(*) 만 실행한다.
   * 프론트의 totalCount 표시에 사용된다.
   */
  int countOrders(SellerOrderListQuery query);
}
