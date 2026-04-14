package com.conk.order.query.service;

import com.conk.order.query.dto.request.OrderKpiQuery;
import com.conk.order.query.dto.response.CurrentRevenueResponse;
import com.conk.order.query.dto.response.MonthlyRevenueAggregationRow;
import com.conk.order.query.dto.response.MonthlyRevenuePointResponse;
import com.conk.order.query.dto.response.OrderKpiResponse;
import com.conk.order.query.dto.response.OutboundStatsResponse;
import com.conk.order.query.dto.response.SellerOrderCountRow;
import com.conk.order.query.dto.response.SellerRevenueResponse;
import com.conk.order.query.dto.response.SellerSkuQuantityRow;
import com.conk.order.query.mapper.OrderKpiQueryMapper;
import com.conk.order.query.mapper.OutboundStatsQueryMapper;
import com.conk.order.query.mapper.RevenueQueryMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 주문 대시보드 집계 조회 서비스.
 *
 * 관리자 대시보드 화면에서 사용하는 집계·통계 조회를 한곳에 묶는다.
 *   - 출고 통계
 *   - 주문 KPI 집계
 */
@Service
public class OrderDashboardQueryService {

  private final OutboundStatsQueryMapper outboundStatsQueryMapper;
  private final OrderKpiQueryMapper orderKpiQueryMapper;
  private final RevenueQueryMapper revenueQueryMapper;
  private final WmsSellerProductClient wmsSellerProductClient;
  private final Clock clock;

  @Autowired
  public OrderDashboardQueryService(
      OutboundStatsQueryMapper outboundStatsQueryMapper,
      OrderKpiQueryMapper orderKpiQueryMapper,
      RevenueQueryMapper revenueQueryMapper,
      WmsSellerProductClient wmsSellerProductClient,
      Clock clock) {
    this.outboundStatsQueryMapper = outboundStatsQueryMapper;
    this.orderKpiQueryMapper = orderKpiQueryMapper;
    this.revenueQueryMapper = revenueQueryMapper;
    this.wmsSellerProductClient = wmsSellerProductClient;
    this.clock = clock;
  }

  public OrderDashboardQueryService(
      OutboundStatsQueryMapper outboundStatsQueryMapper,
      OrderKpiQueryMapper orderKpiQueryMapper,
      Clock clock) {
    this(outboundStatsQueryMapper, orderKpiQueryMapper, null, null, clock);
  }

  /* 출고 통계를 조회한다. */
  public OutboundStatsResponse getOutboundStats() {
    LocalDate today = LocalDate.now(clock);
    int todayCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(today);

    DayOfWeek dayOfWeek = today.getDayOfWeek();
    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
      return OutboundStatsResponse.builder()
          .pendingOutboundCount(todayCount)
          .build();
    }

    LocalDate prevWorkday = dayOfWeek == DayOfWeek.MONDAY
        ? today.minusDays(3)
        : today.minusDays(1);

    int prevCount = outboundStatsQueryMapper.countPendingOutboundOrdersByDate(prevWorkday);
    int delta = todayCount - prevCount;

    String trend = delta > 0 ? "+" + delta : String.valueOf(delta);
    String trendType = delta > 0 ? "up" : delta < 0 ? "down" : "flat";

    return OutboundStatsResponse.builder()
        .pendingOutboundCount(todayCount)
        .trend(trend)
        .trendLabel("전 영업일 대비")
        .trendType(trendType)
        .build();
  }

  /* 기간 내 주문 KPI 를 집계해 반환한다. */
  public OrderKpiResponse getKpi(OrderKpiQuery query) {
    return new OrderKpiResponse(
        orderKpiQueryMapper.countTotal(query),
        orderKpiQueryMapper.countReceived(query),
        orderKpiQueryMapper.countAllocated(query),
        orderKpiQueryMapper.countOutboundInstructed(query),
        orderKpiQueryMapper.countPicking(query),
        orderKpiQueryMapper.countPacking(query),
        orderKpiQueryMapper.countOutboundPending(query),
        orderKpiQueryMapper.countOutboundCompleted(query),
        orderKpiQueryMapper.countCanceled(query)
    );
  }

  public CurrentRevenueResponse getCurrentRevenue() {
    YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
    YearMonth previousMonth = currentMonth.minusMonths(1);

    long currentRevenue = calculateRevenue(currentMonth);
    long previousRevenue = calculateRevenue(previousMonth);

    double percentage = previousRevenue > 0
        ? ((double) (currentRevenue - previousRevenue) / previousRevenue) * 100
        : currentRevenue > 0 ? 100.0 : 0.0;

    String trend = String.format(Locale.US, "%+.1f%%", percentage);
    String trendType = percentage > 0 ? "up" : percentage < 0 ? "down" : "neutral";

    return CurrentRevenueResponse.builder()
        .totalRevenue(currentRevenue)
        .trend(trend)
        .trendLabel("전월 대비")
        .trendType(trendType)
        .build();
  }

  public List<MonthlyRevenuePointResponse> getMonthlyRevenue() {
    YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
    YearMonth firstMonth = currentMonth.minusMonths(5);

    LocalDateTime startDateTime = firstMonth.atDay(1).atStartOfDay();
    LocalDateTime endDateTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
    List<MonthlyRevenueAggregationRow> monthlyRows =
        revenueQueryMapper.findMonthlySellerSkuQuantitiesBetween(startDateTime, endDateTime);

    Map<String, BigDecimal> priceBySellerSku = loadPriceMap(buildPriceKeys(monthlyRows));

    Map<String, Long> revenueByMonth = new LinkedHashMap<>();
    for (MonthlyRevenueAggregationRow row : monthlyRows) {
      long amount = multiply(
          priceBySellerSku.getOrDefault(buildPriceKey(row.getSellerId(), row.getSku()), BigDecimal.ZERO),
          row.getQuantity()
      );
      revenueByMonth.merge(row.getMonth(), amount, Long::sum);
    }

    List<MonthlyRevenuePointResponse> responses = new ArrayList<>();
    YearMonth month = firstMonth;
    while (!month.isAfter(currentMonth)) {
      String monthText = month.toString();
      responses.add(MonthlyRevenuePointResponse.builder()
          .month(monthText)
          .label(month.getMonthValue() + "월")
          .revenue(revenueByMonth.getOrDefault(monthText, 0L))
          .build());
      month = month.plusMonths(1);
    }
    return responses;
  }

  public List<SellerRevenueResponse> getSellerRevenue() {
    YearMonth currentMonth = YearMonth.from(LocalDate.now(clock));
    LocalDateTime startDateTime = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime endDateTime = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

    List<SellerSkuQuantityRow> quantityRows = revenueQueryMapper.findSellerSkuQuantitiesBetween(startDateTime, endDateTime);
    Map<String, BigDecimal> priceBySellerSku = loadPriceMap(buildPriceKeys(quantityRows));

    Map<String, Long> revenueBySeller = new LinkedHashMap<>();
    for (SellerSkuQuantityRow row : quantityRows) {
      long amount = multiply(
          priceBySellerSku.getOrDefault(buildPriceKey(row.getSellerId(), row.getSku()), BigDecimal.ZERO),
          row.getQuantity()
      );
      revenueBySeller.merge(row.getSellerId(), amount, Long::sum);
    }

    Map<String, Integer> orderCountBySeller = new LinkedHashMap<>();
    for (SellerOrderCountRow row : revenueQueryMapper.findSellerOrderCountsBetween(startDateTime, endDateTime)) {
      orderCountBySeller.put(row.getSellerId(), row.getTotalOrders() == null ? 0 : row.getTotalOrders());
    }

    return revenueBySeller.entrySet()
        .stream()
        .map(entry -> {
          int totalOrders = orderCountBySeller.getOrDefault(entry.getKey(), 0);
          long monthRevenue = entry.getValue();
          long avgOrderValue = totalOrders > 0 ? Math.round((double) monthRevenue / totalOrders) : 0L;
          return SellerRevenueResponse.builder()
              .sellerId(entry.getKey())
              .monthRevenue(monthRevenue)
              .totalOrders(totalOrders)
              .avgOrderValue(avgOrderValue)
              .build();
        })
        .sorted(Comparator.comparingLong(SellerRevenueResponse::getMonthRevenue).reversed())
        .toList();
  }

  private long calculateRevenue(YearMonth billingMonth) {
    LocalDateTime startDateTime = billingMonth.atDay(1).atStartOfDay();
    LocalDateTime endDateTime = billingMonth.plusMonths(1).atDay(1).atStartOfDay();
    List<SellerSkuQuantityRow> quantityRows = revenueQueryMapper.findSellerSkuQuantitiesBetween(startDateTime, endDateTime);
    Map<String, BigDecimal> priceBySellerSku = loadPriceMap(buildPriceKeys(quantityRows));

    return quantityRows.stream()
        .mapToLong(row -> multiply(
            priceBySellerSku.getOrDefault(buildPriceKey(row.getSellerId(), row.getSku()), BigDecimal.ZERO),
            row.getQuantity()
        ))
        .sum();
  }

  private Map<String, BigDecimal> loadPriceMap(Iterable<String> sellerSkuKeys) {
    Map<String, BigDecimal> priceBySellerSku = new LinkedHashMap<>();
    Map<String, List<String>> skuKeysBySeller = new LinkedHashMap<>();

    if (wmsSellerProductClient == null) {
      return priceBySellerSku;
    }

    for (String key : sellerSkuKeys) {
      String[] parts = key.split("::", 2);
      if (parts.length < 2) {
        continue;
      }
      skuKeysBySeller.computeIfAbsent(parts[0], ignored -> new ArrayList<>()).add(parts[1]);
    }

    for (Map.Entry<String, List<String>> entry : skuKeysBySeller.entrySet()) {
      String sellerId = entry.getKey();
      List<String> requestedSkus = entry.getValue();
      try {
        WmsSellerProductClient.WmsApiResponse<List<WmsSellerProductClient.WmsSellerProductItem>> response =
            wmsSellerProductClient.getSellerProducts(sellerId, sellerId, sellerId);

        if (response == null || !response.isSuccess() || response.getData() == null) {
          continue;
        }

        for (WmsSellerProductClient.WmsSellerProductItem item : response.getData()) {
          if (item == null || item.getSku() == null || item.getSalePrice() == null) {
            continue;
          }
          if (requestedSkus.contains(item.getSku())) {
            priceBySellerSku.put(buildPriceKey(sellerId, item.getSku()), item.getSalePrice());
          }
        }
      } catch (Exception ignored) {
        // Revenue cards should still render even if one seller's product metadata cannot be loaded.
      }
    }
    return priceBySellerSku;
  }

  private List<String> buildPriceKeys(List<? extends Object> rows) {
    List<String> keys = new ArrayList<>();
    for (Object row : rows) {
      if (row instanceof SellerSkuQuantityRow sellerSkuQuantityRow) {
        keys.add(buildPriceKey(sellerSkuQuantityRow.getSellerId(), sellerSkuQuantityRow.getSku()));
        continue;
      }
      if (row instanceof MonthlyRevenueAggregationRow monthlyRevenueAggregationRow) {
        keys.add(buildPriceKey(monthlyRevenueAggregationRow.getSellerId(), monthlyRevenueAggregationRow.getSku()));
      }
    }
    return keys;
  }

  private String buildPriceKey(String sellerId, String sku) {
    return sellerId + "::" + sku;
  }

  private long multiply(BigDecimal unitPrice, Integer quantity) {
    if (unitPrice == null || quantity == null) {
      return 0L;
    }
    return unitPrice.multiply(BigDecimal.valueOf(quantity.longValue())).longValue();
  }
}
