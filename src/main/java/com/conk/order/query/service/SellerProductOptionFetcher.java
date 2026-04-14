package com.conk.order.query.service;

import com.conk.order.query.dto.response.SellerOrderOptionsResponse.ProductOption;
import java.util.List;

/*
 * 셀러 주문 등록 옵션용 상품 목록 공급자.
 *
 * 현재 order 서비스는 상품 마스터를 직접 보유하지 않으므로 외부 소스(WMS)에서 읽어온다.
 */
public interface SellerProductOptionFetcher {

  List<ProductOption> fetchProducts(String sellerId);
}
