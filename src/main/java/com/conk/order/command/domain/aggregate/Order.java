package com.conk.order.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * 주문 애그리거트 루트.
 *
 * 하나의 주문은 하나 이상의 주문 항목(OrderItem)과 배송지(ShippingAddress)를 포함한다.
 * 외부에서 직접 생성자를 호출하지 못하게 막고,
 * create() 팩토리 메서드를 통해 생성 규칙을 강제한다.
 * createdAt/updatedAt 은 JPA Auditing 이 자동으로 채운다.
 * 물리 테이블: sales_order
 */
@Getter
@Entity
@Table(name = "sales_order")
@EntityListeners(AuditingEntityListener.class)
public class Order {

  /** 주문번호. sales_order.order_id */
  @Id
  @Column(name = "order_id")
  private String orderId;

  /** 주문 일시. sales_order.ordered_at */
  private LocalDateTime orderedAt;

  /** 주문 상태. sales_order.status */
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  /** 주문 항목 목록. sales_order_item 참조 */
  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> items = new ArrayList<>();

  /** 배송지 정보. sales_order 내 ship_to_* 컬럼 */
  @Embedded
  private ShippingAddress shippingAddress;

  /** 셀러 식별자. sales_order.seller_id */
  private String sellerId;

  /** 테넌트 식별자. sales_order.tenant_id */
  @Column(name = "tenant_id")
  private String tenantId;

  /*
   * 창고 식별자. sales_order.warehouse_id
   * Warehouse 도메인 소유권 확인 전까지 참조값(String) 으로 관리한다.
   * WHM 주문 목록 조회(ORD-007) 필터로 사용된다.
   */
  @Column(name = "warehouse_id")
  private String warehouseId;

  /** 운송장 코드. sales_order.tracking_code */
  @Column(name = "tracking_code")
  private String trackingCode;

  /** 소채널 주문번호. sales_order.channel_order_no */
  private String channelOrderNo;

  /** 판매 채널. sales_order.order_channel */
  @Enumerated(EnumType.STRING)
  private OrderChannel orderChannel;

  /** 수령인 이름. sales_order.receiver_name */
  private String receiverName;

  /** 수령인 연락처. sales_order.receiver_phone_no */
  private String receiverPhoneNo;

  /** 메모. sales_order.memo */
  private String memo;

  /** 출고 완료 일시. sales_order.shipped_at */
  private LocalDateTime shippedAt;

  /** 등록 일시. sales_order.created_at — JPA Auditing 자동 세팅. */
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  /** 수정 일시. sales_order.updated_at — JPA Auditing 자동 세팅. */
  @LastModifiedDate
  private LocalDateTime updatedAt;

  /** 등록자. sales_order.created_by */
  private String createdBy;

  /** 수정자. sales_order.updated_by */
  private String updatedBy;

  protected Order() {}

  private Order(
      String orderId,
      LocalDateTime orderedAt,
      String sellerId,
      String tenantId,
      OrderChannel orderChannel,
      List<OrderItem> items,
      ShippingAddress shippingAddress,
      String receiverName,
      String receiverPhoneNo,
      String memo,
      OrderStatus status
  ) {
    validateOrderId(orderId);
    validateOrderedAt(orderedAt);
    validateSellerId(sellerId);
    validateTenantId(tenantId);
    validateItems(items);
    validateShippingAddress(shippingAddress);
    this.orderId = orderId;
    this.orderedAt = orderedAt;
    this.sellerId = sellerId;
    this.tenantId = tenantId;
    this.orderChannel = orderChannel;
    this.shippingAddress = shippingAddress;
    this.receiverName = receiverName;
    this.receiverPhoneNo = receiverPhoneNo;
    this.memo = memo;
    this.status = status;

    for (OrderItem item : items) {
      addItem(item);
    }
  }

  /**
   * 주문 생성 팩토리 메서드.
   * 새 주문은 항상 RECEIVED(접수) 상태로 시작한다.
   *
   * @param orderId        주문번호
   * @param orderedAt      주문 일시
   * @param sellerId       셀러 식별자
   * @param orderChannel   판매 채널
   * @param items          주문 항목 목록
   * @param shippingAddress 배송지
   * @param receiverName   수령인 이름
   * @param receiverPhoneNo 수령인 연락처
   * @param memo           메모
   * @return Order
   */
  public static Order create(
      String orderId,
      LocalDateTime orderedAt,
      String sellerId,
      String tenantId,
      OrderChannel orderChannel,
      List<OrderItem> items,
      ShippingAddress shippingAddress,
      String receiverName,
      String receiverPhoneNo,
      String memo
  ) {
    return new Order(
        orderId,
        orderedAt,
        sellerId,
        tenantId,
        orderChannel,
        items,
        shippingAddress,
        receiverName,
        receiverPhoneNo,
        memo,
        OrderStatus.RECEIVED
    );
  }

  /*
   * 접수 상태 여부를 반환한다.
   * 출고 대기 건수 집계 시 RECEIVED 상태 주문을 필터링하는 데 사용된다.
   */
  public boolean isReceived() {
    return status == OrderStatus.RECEIVED;
  }

  /*
   * 주문을 출고 완료 상태로 변경한다.
   * 단, 이미 취소된 주문은 출고 완료 처리할 수 없다.
   */
  public void markOutboundCompleted() {
    if (status == OrderStatus.CANCELED) {
      throw new IllegalStateException("Canceled order cannot be completed.");
    }
    this.status = OrderStatus.OUTBOUND_COMPLETED;
    this.shippedAt = LocalDateTime.now();
  }

  /*
   * 주문을 취소 상태로 변경한다.
   * RECEIVED, ALLOCATED 상태일 때만 취소할 수 있다.
   */
  public void cancelOrder() {
    if (status != OrderStatus.RECEIVED && status != OrderStatus.ALLOCATED) {
      throw new IllegalStateException("Order cannot be canceled in current status.");
    }
    this.status = OrderStatus.CANCELED;
  }

  /*
   * 주문 상태를 target 으로 변경한다.
   * OrderStatus.canTransitionTo() 에 정의된 전이 규칙을 따른다.
   * OUTBOUND_COMPLETED 로 전이 시 shippedAt 을 현재 시각으로 기록한다.
   *
   * @param target 변경할 상태
   * @throws IllegalStateException 허용되지 않는 전이인 경우
   */
  public void changeStatus(OrderStatus target) {
    if (!this.status.canTransitionTo(target)) {
      throw new IllegalStateException(
          String.format("Cannot transition from %s to %s", this.status, target));
    }
    this.status = target;
    if (target == OrderStatus.OUTBOUND_COMPLETED) {
      this.shippedAt = LocalDateTime.now();
    }
  }

  /*
   * 창고를 주문에 배정한다.
   * 재고 할당(ALLOCATED) 단계에서 담당 창고가 결정될 때 호출된다.
   * ORD-007 WHM 주문 목록 조회의 warehouseId 필터 기준값으로 사용된다.
   */
  public void assignWarehouse(String warehouseId) {
    this.warehouseId = warehouseId;
  }

  private void addItem(OrderItem item) {
    item.assignOrder(this);
    this.items.add(item);
  }

  /**
   * 주문번호 필수값 검증.
   *
   * @param orderId 주문번호
   */
  private void validateOrderId(String orderId) {
    if (orderId == null || orderId.isBlank()) {
      throw new IllegalArgumentException("Order number is required.");
    }
  }

  /**
   * 주문 일시 필수값 검증.
   *
   * @param orderedAt 주문 일시
   */
  private void validateOrderedAt(LocalDateTime orderedAt) {
    if (orderedAt == null) {
      throw new IllegalArgumentException("Order date is required.");
    }
  }

  /**
   * 셀러 식별자 필수값 검증.
   *
   * @param sellerId 셀러 식별자
   */
  private void validateSellerId(String sellerId) {
    if (sellerId == null || sellerId.isBlank()) {
      throw new IllegalArgumentException("Seller ID is required.");
    }
  }

  /**
   * 테넌트 식별자 필수값 검증.
   *
   * @param tenantId 테넌트 식별자
   */
  private void validateTenantId(String tenantId) {
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("Tenant ID is required.");
    }
  }

  /**
   * 주문 항목 필수값 검증.
   *
   * @param items 주문 항목 목록
   */
  private void validateItems(List<OrderItem> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Order items are required.");
    }
  }

  /**
   * 배송지 필수값 검증.
   *
   * @param shippingAddress 배송지
   */
  private void validateShippingAddress(ShippingAddress shippingAddress) {
    if (shippingAddress == null) {
      throw new IllegalArgumentException("Shipping address is required.");
    }
  }
}
