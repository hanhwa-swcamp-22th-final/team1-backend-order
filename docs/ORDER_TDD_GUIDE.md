# CONK ORDER 도메인 — TDD 개발 가이드

> 이 문서는 ORDER 도메인(9개 API) 구현 시 참고용입니다.

---

## 전체 진행 흐름

``` 
Phase 0: 엔티티 / enum / 연관관계 선도출 → 구조 고정
Phase 1: 셀러 주문 등록 (ORD-002, ORD-003) → 데이터 Create
Phase 2: 주문 목록 조회 (ORD-004, ORD-005, ORD-007) → 역할별 Read
Phase 3: 대시보드 통계 (ORD-001, ORD-006) → MyBatis 집계
Phase 4: 매출 집계 (ORD-008, ORD-009) → MyBatis 집계
```

Phase 0 완료 후 각 기능은 **Repository → Service → Controller** 순서(Inside-Out)로,
**Red(실패 테스트) → Green(최소 구현) → Refactor** 사이클을 반복.

---

## TDD 규칙

1. 프로덕션 코드 전에 반드시 실패하는 테스트부터 작성
2. 한 테스트 통과하면 다음으로 이동
3. Green 상태마다 커밋: `test:`, `feat:`, `refactor:`
4. Happy Path → Edge Case → Error Case 순서
5. 테스트 명명: `@DisplayName("한글 설명")` + 메서드는 `should_동작_when_조건`
6. 현재 프로젝트는 기능별 테스트 전에 엔티티, enum, 연관관계를 먼저 정리한다.

---

## API 명세 요약 (9개)

### ORD-001 — 대시보드 출고 통계
- **GET** `/orders/outbound/stats`
- **인증**: bearer
- **응답**: `{ success, data: { pendingOutboundCount, trend, trendLabel, trendType } }`
- **로직**: status가 `RECEIVED` 인 건수 집계 + 직전 영업일 대비 추이
  - 화~금: 전날 대비
  - 월요일: 직전 금요일 대비
  - 토/일: 추이 필드 미계산
- **구현**: MyBatis

### ORD-002 — 셀러 단건 주문 등록
- **POST** `/orders/seller/manual`
- **인증**: bearer
- **요청 body**:
  ```
  orderNo, salesChannel, recipient, contact,
  address1, address2(선택), city, state, zipCode,
  orderDate, memo,
  items[]: { sku, quantity }
  ```
- **응답**: `{ success, message: "created", data: { id, orderNo, channel, recipient, address, itemsSummary, orderedAt, status, trackingNo, canCancel, detail: { receiverPhone, state, city, zipCode, addressLine, shippingMethod, carrier, memo, items[]: { sku, productName, quantity, unitPrice } } } }`
- **로직**: 중복 orderNo → 409, 초기 status = RECEIVED, canCancel = status.isCancellable()
- **itemsSummary 형식**: `"LB-AMP-30 × 2, LB-MSK-5P × 1"`

### ORD-003 — 셀러 일괄 주문 등록
- **POST** `/orders/seller/bulk`
- **인증**: bearer
- **요청 body**: `orders[]: { orderNo, sku, quantity, recipient, contact, address, city, state, zipCode, orderDate, memo }`
- **응답**: `{ success, message: "created", data: { savedCount } }`

### ORD-004 — 셀러 주문 목록
- **GET** `/orders/seller/list`
- **인증**: bearer (SELLER 권한)
- **응답**: ORD-002 응답과 동일 구조의 배열
- **로직**: 본인 sellerId 기준 필터링

### ORD-005 — 관리자 주문 목록
- **GET** `/orders/list`
- **인증**: bearer (MASTER_ADMIN 권한)
- **응답**: `data[]: { id, channel, company(seller조인), warehouse(warehouse조인), skuCount, qty, destState, orderedAt, status }`

### ORD-006 — 주문 KPI
- **GET** `/orders/kpi`
- **인증**: bearer
- **응답**: `{ success, data: { todayTotal, pendingCount, pickingCount, shippedCount } }`
- **로직**:
  - todayTotal = 금일 주문 건수
  - pendingCount = RECEIVED + ALLOCATED
  - pickingCount = PICKING_PACKING
  - shippedCount = OUTBOUND_COMPLETED (금일 shipped_at 기준)
- **구현**: MyBatis

### ORD-007 — 창고관리자 주문 목록
- **GET** `/orders/whm`
- **인증**: bearer (WH_MANAGER 권한)
- **응답**: `data[]: { id, channel, seller(대표자명), company(셀러명), product("앰플 세럼 30ml × 2" 형식), region("CA, USA"), orderedAt, warehouse(창고명), status }`

### ORD-008 — 당월 매출
- **GET** `/orders/revenue/current`
- **인증**: bearer
- **응답**: `{ success, data: { totalRevenue, trend, trendLabel, trendType } }`
- **로직**: SUM(sales_order_item.quantity × product.sale_price_amt) + 전월 대비 %
- **구현**: MyBatis

### ORD-009 — 월별 매출 추이
- **GET** `/orders/revenue/monthly`
- **인증**: bearer
- **응답**: `data[]: { month("2025-10"), label("10월"), revenue }`
- **로직**: 최근 6개월 GROUP BY 월별
- **구현**: MyBatis

---

## ERD 핵심 정보

### OrderStatus (ERD 실제 값)
```
RECEIVED → ALLOCATED → OUTBOUND_INSTRUCTED → PICKING_PACKING → OUTBOUND_PENDING → OUTBOUND_COMPLETED
                                                                                → CANCELED
```
- **canCancel**: RECEIVED, ALLOCATED에서만 true
- **pendingOutboundCount**: 현재 프로젝트 규칙상 `RECEIVED`만 집계

### OrderChannel
```
AMAZON / MANUAL / EXCEL / SHOPIFY
```

### 테이블 관계
```
sales_order (1) ──── (N) sales_order_item   [order_id FK]
sales_order (N) ──── (1) seller              [seller_id FK, VARCHAR]
sales_order (N) ──── (1) warehouse           [warehouse_id FK, VARCHAR]
sales_order (1) ──── (1) shipment_invoice    [invoice_no FK]
sales_order_item ─── product                 [sku_id 조인, 매출계산용]
```

- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 소유 도메인 확인 전까지 로컬 엔티티로 확정하지 않는다.
- 우선 Order 도메인에서는 `sellerId`, `warehouseId`, `invoiceNo`, `sku` 같은 참조 값으로만 다룬다.
- 물리 테이블명은 DB 소유권 확인 전까지 가이드의 ERD 명칭을 참고만 하고, 구현 시점에 최종 확정한다.

### 주요 컬럼 타입 주의
| 컬럼 | 타입 | 비고 |
|------|------|------|
| order_id | VARCHAR(255) | PK, 문자열 |
| seller_id | VARCHAR(255) | FK, Long 아님 |
| warehouse_id | VARCHAR(255) | FK, Long 아님 |
| invoice_no | VARCHAR(255) | shipment_invoice PK |
| picked_quantity | INT DEFAULT 0 | |
| packed_quantity | INT DEFAULT 0 | ERD에서 확인 |
| memo | VARCHAR(255) | sales_order에 존재 |

---

## Phase별 TDD 사이클 상세

### Phase 0 — 엔티티 / enum / 연관관계 선도출

```
[Model]
  ① OrderStatus 전체 상태값 정의
  ② Order / OrderItem / ShippingAddress 필드 확장
  ③ 참조 식별자 sellerId, warehouseId, invoiceNo, sku 를 String 으로 고정
  ④ 외부 도메인 후보 엔티티는 로컬 엔티티 생성 대신 참조 필드로 유지
  ⑤ 테이블/컬럼 매핑은 DB 소유권 확인 전까지 과도하게 확정하지 않음
```

### Phase 1 — ORD-002 단건 등록

```
[Repository]
  Red  ① save() + findById() 테스트
  Green② Entity + Repository 구현
  Red  ③ cascade ALL로 items도 저장되는지
  Green④ 연관관계 매핑

[Service]
  Red  ⑤ 정상 요청 → SellerOrderResponse 반환
  Green⑥ OrderService.createSellerOrder() 구현
  Red  ⑦ 중복 orderNo → 409 ConflictException
  Green⑧ existsByOrderId() 체크
  Red  ⑨ 빈 items → 400 예외
  Green⑩ Validation
  Red  ⑪ 초기 status = RECEIVED
  Green⑫ 기본값 설정
  Refactor ⑬ itemsSummary, canCancel 로직 분리

[Controller]
  Red  ⑭ POST → 201 + success/message/data
  Green⑮ Controller 연결
  Red  ⑯ 필수 필드 누락 → 400
  Green⑰ @Valid + GlobalExceptionHandler
```

### Phase 2 — ORD-004 셀러 목록

```
[Repository]
  Red  ① sellerId 기준 조회
  Green② findBySellerId()
  Red  ③ 다른 셀러 주문은 안 나오는 격리 테스트
  Green④ 통과 확인

[Service]
  Red  ⑤ 목록 → SellerOrderResponse[] 매핑
  Green⑥ getSellerOrderList()
  Red  ⑦ canCancel: RECEIVED→true, OUTBOUND_INSTRUCTED→false
  Green⑧ OrderStatus.isCancellable()
  Red  ⑨ itemsSummary 포맷 검증
  Green⑩ items → summary 변환
  Red  ⑪ detail.addressLine = address1 + address2
  Green⑫ 주소 조합
  Red  ⑬ carrier = shipmentInvoice.carrierType
  Green⑭ 조인 처리

[Controller]
  Red  ⑮ GET → 200 + success/data
  Green⑯ Controller 연결
```

### Phase 3 — ORD-001, ORD-006 (MyBatis)

```
[Mapper]
  Red  ① 출고 대기 건수 집계
  Green② SQL 작성
  Red  ③ 전주 대비 추이 계산
  Green④ 서브쿼리
  Red  ⑤ KPI: 금일 주문 + status별 집계
  Green⑥ CASE WHEN + COALESCE

[Service → Controller]
  Red  ⑦ 응답 매핑
  Green⑧ 연결
```

### Phase 4 — ORD-008, ORD-009 (MyBatis)

```
[Mapper]
  Red  ① 당월 매출 = SUM(qty × price) + product 조인
  Green② SQL
  Red  ③ 전월 대비 % 계산
  Green④ 서브쿼리
  Red  ⑤ 월별 6개월 GROUP BY
  Green⑥ DATE_FORMAT

[Service → Controller]
  Red  ⑦ 응답 매핑
  Green⑧ 연결
```

---

## 커밋 컨벤션

```
test: ORD-002 SalesOrder 저장/조회 Repository 테스트 추가
feat: SalesOrder Entity 및 JPA Repository 구현
test: ORD-002 셀러 단건 주문 등록 Service 테스트 추가
feat: OrderService.createSellerOrder() 구현
refactor: itemsSummary 생성 로직 유틸 클래스로 추출
test: ORD-004 셀러 주문 목록 조회 테스트 추가
feat: OrderController GET /orders/seller/list 구현
```
