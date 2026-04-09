package com.conk.order.query.application.dto;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

/*
 * 셀러 주문 상세 조회 응답 DTO.
 *
 * GET /orders/seller/{orderId} 응답으로 사용한다.
 * OrderDetailResponse 와 유사하지만 canCancel 필드를 추가로 포함한다.
 * canCancel 은 RECEIVED 또는 ALLOCATED 상태일 때 true 이다.
 */
@Getter
public class SellerOrderDetailResponse {

  private final String orderId;
  private final LocalDateTime orderedAt;
  private final OrderStatus status;
  private final OrderChannel orderChannel;
  private final String receiverName;
  private final String phone;
  private final String memo;
  private final String street1;
  private final String street2;
  private final String state;
  private final String zip;
  private final String country;

  /** 취소 가능 여부. RECEIVED 또는 ALLOCATED 상태일 때 true. */
  private final boolean canCancel;

  private final List<ItemDetail> items;

  private SellerOrderDetailResponse(Order order) {
    this.orderId = order.getOrderId();
    this.orderedAt = order.getOrderedAt();
    this.status = order.getStatus();
    this.orderChannel = order.getOrderChannel();
    this.receiverName = order.getReceiverName();
    this.phone = order.getReceiverPhoneNo();
    this.memo = order.getMemo();
    this.street1 = order.getShippingAddress().getAddress1();
    this.street2 = order.getShippingAddress().getAddress2();
    this.state = order.getShippingAddress().getState();
    this.zip = order.getShippingAddress().getZipCode();
    this.country = order.getShippingAddress().getCountry();
    this.canCancel = order.getStatus() == OrderStatus.RECEIVED
        || order.getStatus() == OrderStatus.ALLOCATED;
    this.items = order.getItems().stream()
        .map(ItemDetail::new)
        .toList();
  }

  /** Order 엔티티로부터 응답 DTO 를 생성한다. */
  public static SellerOrderDetailResponse from(Order order) {
    return new SellerOrderDetailResponse(order);
  }

  /** 주문 항목 상세. */
  @Getter
  public static class ItemDetail {
    private final String sku;
    private final int quantity;
    private final String productName;

    private ItemDetail(OrderItem item) {
      this.sku = item.getSku();
      this.quantity = item.getQuantity();
      this.productName = item.getProductNameSnapshot();
    }
  }
}
