package com.conk.order.query.mapper;

import com.conk.order.query.dto.response.MonthlyRevenueAggregationRow;
import com.conk.order.query.dto.response.SellerOrderCountRow;
import com.conk.order.query.dto.response.SellerSkuQuantityRow;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RevenueQueryMapper {

  List<SellerSkuQuantityRow> findSellerSkuQuantitiesBetween(
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime
  );

  List<SellerOrderCountRow> findSellerOrderCountsBetween(
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime
  );

  List<MonthlyRevenueAggregationRow> findMonthlySellerSkuQuantitiesBetween(
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime
  );
}
