package com.conk.order.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

/*
 * 주문 항목 엔티티.
 *
 * 한 주문 안에 포함되는 개별 상품 단위를 표현한다.
 * SKU와 수량은 필수값이며, 상품명 스냅샷은 주문 시점의 상품명을 보존한다.
 * createdAt/updatedAt 은 JPA Auditing 이 자동으로 채운다.
 * 물리 테이블: sales_order_item
 */
@Getter
@Entity
@Table(name = "sales_order_item")
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

  /** 주문 항목 식별자. 자동 생성. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 소속 주문. sales_order_item.order_id */
  @ManyToOne
  @JoinColumn(name = "order_id")
  private Order order;

  /** SKU 코드. sales_order_item.sku_id */
  @Column(name = "sku_id")
  private String sku;

  /** 주문 수량. sales_order_item.quantity */
  private int quantity;

  /** 주문 시점 상품명 스냅샷. sales_order_item.product_name_snapshot */
  @Column(name = "product_name_snapshot")
  private String productNameSnapshot;

  /** 피킹 수량. sales_order_item.picked_quantity */
  @Column(name = "picked_quantity")
  private int pickedQuantity = 0;

  /** 패킹 수량. sales_order_item.packed_quantity */
  @Column(name = "packed_quantity")
  private int packedQuantity = 0;

  /** 등록 일시. sales_order_item.created_at — JPA Auditing 자동 세팅. */
  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  /** 수정 일시. sales_order_item.updated_at — JPA Auditing 자동 세팅. */
  @LastModifiedDate
  private LocalDateTime updatedAt;

  /** 등록자. sales_order_item.created_by */
  private String createdBy;

  /** 수정자. sales_order_item.updated_by */
  private String updatedBy;

  protected OrderItem() {}

  private OrderItem(String sku, int quantity, String productNameSnapshot) {
    validateSku(sku);
    validateQuantity(quantity);
    this.sku = sku;
    this.quantity = quantity;
    this.productNameSnapshot = productNameSnapshot;
  }

  /**
   * 주문 항목 생성 팩토리 메서드.
   *
   * @param sku                 SKU 코드 (필수)
   * @param quantity            주문 수량 (필수, 1 이상)
   * @param productNameSnapshot 주문 시점 상품명 스냅샷 (선택)
   * @return OrderItem
   */
  public static OrderItem create(String sku, int quantity, String productNameSnapshot) {
    return new OrderItem(sku, quantity, productNameSnapshot);
  }

  /* 소속 주문을 연결한다. Order.addItem() 에서만 호출한다. */
  void assignOrder(Order order) {
    this.order = order;
  }

  /**
   * SKU 코드 필수값 검증.
   *
   * @param sku SKU 코드
   */
  private void validateSku(String sku) {
    if (sku == null || sku.isBlank()) {
      throw new IllegalArgumentException("SKU is required.");
    }
  }

  /**
   * 주문 수량 최솟값 검증.
   *
   * @param quantity 주문 수량
   */
  private void validateQuantity(int quantity) {
    if (quantity < 1) {
      throw new IllegalArgumentException("Quantity must be greater than zero.");
    }
  }
}
