# Order 트러블슈팅

## 기록 규칙

- 디버깅이나 오류 분석 요청을 처리할 때마다 항목을 누적 작성한다.
- 핵심은 `원인`과 `해결`이 바로 보이게 적는 것이다.
- 필요하면 영향 범위, 재현 방법, 검증 결과까지 함께 적는다.
- 항목은 최신 이슈를 아래에 추가하는 방식으로 관리한다.

## 작성 템플릿

### [이슈 제목]

**Problem**  
- 어떤 문제가 발생했는지 적는다.

**Impact**  
- 개발이나 실행에 어떤 영향이 있었는지 적는다.

**Reproduction**  
- 재현 조건이나 재현 순서를 적는다.

**Cause**  
- 확인된 원인을 적는다.

**Fix**  
- 적용한 해결 방법을 적는다.

**Verification**  
- 해결 후 무엇으로 확인했는지 적는다.

**Prevention**  
- 다음에 같은 문제를 줄이기 위한 체크 포인트를 적는다.

**Related**  
- 관련 파일이나 브랜치를 적는다.

## 2026-03-27

### [ORD-001 query 계층 컴파일 오류]

**Problem**  
- `OutboundStatsQueryDao`, `OutboundStatsQueryService`, `OutboundStatsResponse`, `OutboundStatsQueryServiceTest` 작성 중 빨간줄이 다수 발생했다.

**Cause**  
- DAO를 `class`로 선언하고 메서드 본문 없이 작성했다.
- Service에 `outboundStatsQueryDao` 필드/생성자가 없었다.
- DTO에 생성자와 getter가 없었다.
- 테스트에서 오타, 잘못된 타입명, 중복 메서드명, 잘못된 assertion 문법이 섞였다.

**Fix**
- DAO는 `interface`로 선언하거나 메서드 본문을 구현한다.
- Service에 DAO 의존성을 필드/생성자로 추가한다.
- DTO에 필요한 생성자와 getter를 추가한다.
- 테스트의 메서드명/타입명/Mockito 문법/assertion 오타를 정리한다.

## 2026-04-05

### [CreateOrderIntegrationTest — existsById 컴파일 모호성 오류]

**Problem**
- `CreateOrderIntegrationTest`에서 `orderRepository.existsById(orderNo)` 호출 시 "reference to existsById is ambiguous" 컴파일 오류 발생.

**Cause**
- `OrderRepository`가 `OrderSavePort`와 `JpaRepository` 두 인터페이스를 상속하는 구조에서, `existsById`가 양쪽 모두에 정의되어 있어 컴파일러가 어느 쪽을 호출할지 결정하지 못함.

**Fix**
- `orderRepository.existsById(orderNo)` → `orderRepository.findById(orderNo).isPresent()`로 변경해 모호성 제거.

**Related**
- `src/test/java/com/conk/order/command/controller/CreateOrderIntegrationTest.java`

---

### [Order.warehouseId 추가 후 MariaDB 컬럼 누락 오류]

**Problem**
- `Order` 엔티티에 `warehouseId` 필드를 추가한 뒤 통합 테스트(`@SpringBootTest`) 실행 시 "Unknown column 'o1_0.warehouse_id' in 'SELECT'" 오류 발생.

**Cause**
- `@SpringBootTest`는 실제 MariaDB에 연결하는데, `sales_order` 물리 테이블에 `warehouse_id` 컬럼이 존재하지 않음. `ddl-auto` 기본값이 `none`이어서 스키마가 자동으로 갱신되지 않음.

**Fix**
- `application-dev.yml`에서 `spring.jpa.hibernate.ddl-auto: update`로 변경하거나 직접 `ALTER TABLE sales_order ADD COLUMN warehouse_id VARCHAR(255)` 실행.

**Verification**
- 미해결 상태. 컬럼 추가 후 통합 테스트 재실행으로 확인 필요.

**Related**
- `src/main/java/com/conk/order/command/domain/aggregate/Order.java`
- `application-dev.yml` (gitignore 대상, 로컬에서 직접 수정)

---

### [SellerOrderListQueryMapper.xml — 컬럼명·테이블명 오류]

**Problem**
- `SellerOrderListQueryMapper.xml`의 `findOrders` 쿼리에서 `o.order_no`와 `LEFT JOIN order_item` 사용으로 실 DB 조회 시 오류 발생.

**Cause**
- `Order` 엔티티의 `orderNo` 필드는 `@Column(name = "order_id")`로 물리 컬럼이 `order_id`인데 XML에서 `order_no`를 사용. 주문 항목 물리 테이블명은 `sales_order_item`인데 XML에서 `order_item`을 사용. fix 커밋이 PR 머지 전에 push되지 않아 dev 브랜치에 반영되지 않음.

**Fix**
- `o.order_no` → `o.order_id` (SELECT·GROUP BY 모두)
- `LEFT JOIN order_item` → `LEFT JOIN sales_order_item`

**Related**
- `src/main/resources/mappers/SellerOrderListQueryMapper.xml`
- `src/main/java/com/conk/order/command/domain/aggregate/Order.java` (L24: `@Column(name = "order_id")`)

---

### [OrderKpiQueryMapper.xml — OrderStatus enum 불일치]

**Problem**
- `OrderKpiQueryMapper.xml`에서 `WHERE status = 'PICKING'`과 `WHERE status = 'PACKING'`으로 조회하지만 `OrderStatus` enum에 해당 값이 존재하지 않아 실 DB 조회 시 항상 0 반환.

**Cause**
- KPI 설계 시 피킹과 패킹이 `PICKING_PACKING`으로 통합된 단일 상태임을 반영하지 않고 XML 작성. `OUTBOUND_INSTRUCTED`와 `OUTBOUND_PENDING` 상태도 KPI에서 누락됨.

**Fix**
- `countPicking(PICKING)` + `countPacking(PACKING)` 제거
- `countOutboundInstructed(OUTBOUND_INSTRUCTED)`, `countPickingPacking(PICKING_PACKING)`, `countOutboundPending(OUTBOUND_PENDING)` 추가
- `OrderKpiResponse`, `OrderKpiQueryMapper`, `OrderKpiQueryService`, 관련 테스트 모두 함께 수정.

**Related**
- `src/main/resources/mappers/OrderKpiQueryMapper.xml`
- `src/main/java/com/conk/order/command/domain/aggregate/OrderStatus.java`
- `src/main/java/com/conk/order/query/dto/OrderKpiResponse.java`
- `src/main/java/com/conk/order/query/mapper/OrderKpiQueryMapper.java`
- `src/main/java/com/conk/order/query/service/OrderKpiQueryService.java`
