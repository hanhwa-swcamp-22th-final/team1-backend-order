package com.conk.order.query.dto.response;

import com.conk.order.command.domain.aggregate.Order;
import com.conk.order.command.domain.aggregate.OrderChannel;
import com.conk.order.command.domain.aggregate.OrderItem;
import com.conk.order.command.domain.aggregate.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

/*
 * 주문 단건 상세 조회 응답 DTO.
 *
 * GET /orders/{orderId} 응답으로 사용한다.
 * Order 엔티티에서 필요한 필드만 추출하여 반환한다.
 * 주문 항목(items)과 배송지 정보를 포함한다.
 */
@Getter
public class OrderDetailResponse {

  /** 주문번호. */
  private final String orderId;

  /** 주문 일시. */
  private final LocalDateTime orderedAt;

  /** 주문 상태. */
  private final OrderStatus status;

  /** 판매 채널. */
  private final OrderChannel orderChannel;

  /** 셀러 식별자. */
  private final String sellerId;

  /** 수령인 이름. */
  private final String receiverName;

  /** 수령인 연락처. */
  private final String phone;

  /** 메모. */
  private final String memo;

  /** 배송지 기본 주소. */
  private final String street1;

  /** 배송지 상세 주소. */
  private final String street2;

  /** 배송지 주/지역. */
  private final String state;

  /** 배송지 우편번호. */
  private final String zip;

  /** 배송지 국가. */
  private final String country;

  /** 창고 식별자. */
  private final String warehouseId;

  /** 주문 항목 목록. */
  private final List<ItemDetail> items;

  private OrderDetailResponse(Order order) {
    this.orderId = order.getOrderId();
    this.orderedAt = order.getOrderedAt();
    this.status = order.getStatus();
    this.orderChannel = order.getOrderChannel();
    this.sellerId = order.getSellerId();
    this.receiverName = order.getReceiverName();
    this.phone = order.getReceiverPhoneNo();
    this.memo = order.getMemo();
    this.street1 = order.getShippingAddress().getAddress1();
    this.street2 = order.getShippingAddress().getAddress2();
    this.state = order.getShippingAddress().getState();
    this.zip = order.getShippingAddress().getZipCode();
    this.country = order.getShippingAddress().getCountry();
    this.warehouseId = order.getWarehouseId();
    this.items = order.getItems().stream()
        .map(ItemDetail::new)
        .toList();
  }

  /** Order 엔티티로부터 응답 DTO 를 생성한다. */
  public static OrderDetailResponse from(Order order) {
    return new OrderDetailResponse(order);
  }

  /** 주문 항목 상세. */
  @Getter
  public static class ItemDetail {
    private final String sku;
    private final int quantity;
    private final String productName;
    private final int pickedQuantity;
    private final int packedQuantity;

    private ItemDetail(OrderItem item) {
      this.sku = item.getSku();
      this.quantity = item.getQuantity();
      this.productName = item.getProductNameSnapshot();
      this.pickedQuantity = item.getPickedQuantity();
      this.packedQuantity = item.getPackedQuantity();
    }
  }
}
