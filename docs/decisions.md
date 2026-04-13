# Order 결정 기록

## 2026-04-01 엔티티 선도출 및 ORD-001 예외 규칙

### Decision
- 전체 진행 순서는 `docs/ORDER_TDD_GUIDE.md`의 Phase 1~4를 따른다.
- 기능별 단위 테스트 전에 Phase 0로 엔티티, enum, 연관관계를 먼저 정리한다.
- `OrderStatus`는 가이드 기준 6단계 흐름을 사용한다.
  - `RECEIVED`, `ALLOCATED`, `OUTBOUND_INSTRUCTED`, `PICKING_PACKING`, `OUTBOUND_PENDING`, `OUTBOUND_COMPLETED`
  - 취소는 별도 상태 `CANCELED`로 둔다.
- `ORD-001`은 가이드 구조를 따르되 추이 계산 규칙은 프로젝트 합의 기준으로 유지한다.
  - 평일은 전날 대비
  - 월요일은 직전 금요일 대비
  - 주말은 추이 필드를 계산하지 않는다.
- `ORD-001`의 `pendingOutboundCount`는 `RECEIVED` 상태만 집계한다.
- `sellerId`, `warehouseId`, `invoiceNo`, `sku` 등 참조 식별자는 모두 `String`으로 둔다.
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 소유 도메인 확인 전까지 로컬 엔티티로 확정하지 않는다.
  - 우선 Order 도메인에서는 참조 ID 또는 조회 조인 대상으로만 다룬다.
- 물리 테이블명은 지금 단계에서 `sales_order`로 강제하지 않는다.
  - DB 소유권과 연동 방식이 확정되면 `orders`와 `sales_order` 중 하나로 맞춘다.

### Context
- 현재 저장소는 최소 도메인과 `ORD-001` 초안까지 구현된 상태다.
- 기존 구현의 상태 모델과 가이드의 상태 모델이 달라 이후 API 구현 시 재작업 가능성이 컸다.
- `ORD-001`의 추이 계산은 팀 내에서 가이드와 다르게 이미 방향을 정했다.
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 다른 도메인의 소유 데이터일 가능성이 있어 엔티티를 섣불리 만들기 어렵다.

### Alternatives Considered
- 현재의 단순 상태 모델과 `orders` 테이블 기준을 그대로 유지한다.
- 가이드를 그대로 따라 상태, 추이, 물리 테이블명까지 한 번에 모두 맞춘다.
- 가이드의 Phase와 상태 모델은 따르되, `ORD-001` 추이 규칙과 외부 도메인 후보 엔티티는 프로젝트 기준으로 예외 처리한다.

### Why
- 상태 모델을 먼저 가이드에 맞추면 `ORD-002`~`ORD-009` 구현 시 DTO, Repository, Mapper, 테스트 재수정 비용을 줄일 수 있다.
- 엔티티 선도출 방식은 이후 기능 테스트의 기반 구조를 먼저 고정하는 데 유리하다.
- 외부 도메인 소유 데이터는 참조 ID로 우선 처리하는 편이 잘못된 Aggregate 경계 설정을 피할 수 있다.
- `ORD-001` 추이 계산은 이미 합의된 제품 규칙이 있으므로 가이드보다 프로젝트 결정을 우선한다.

## 2026-04-09 Bulk 업로드 대용량 처리 방식

### Decision
- `ORD-003` 대용량 업로드는 JDBC batch 설정 대신 `flush + clear` 기반 메모리 관리로 처리한다.
- 현재 구현은 `BulkOrderCommandService.create()`가 `saveOrder()` 흐름을 유지하면서 업로드 제한값과 `flush/clear` 주기를 `order.bulk-upload.*` 설정으로 관리한다.
- `hibernate.jdbc.batch_size`, `hibernate.order_inserts` 설정은 사용하지 않는다.
- 기본 설정은 `max-row-limit=5000`, `flush-interval=500`으로 둔다.

### Context
- Bulk 업로드 처리에 `saveAll + hibernate.jdbc.batch_size`를 적용하는 시도가 있었지만, 이번 작업에서는 배치 최적화보다 부분 저장 정책과 현재 서비스 구조를 유지하는 쪽이 우선이었다.
- 기존 서비스는 부분 저장 정책과 행 단위 실패 수집을 중심으로 작성되어 있었다.
- 대량 업로드 시 영속성 컨텍스트 누적에 따른 메모리 사용량 증가를 완화할 필요가 있었다.

### Alternatives Considered
- `saveAll`과 Hibernate JDBC batch 설정을 유지한다.
- 서비스 전체를 하나의 트랜잭션으로 묶고 `EntityManager.persist()` 기반으로 chunk 처리한다.
- 현재 저장 흐름을 유지하면서 `flush + clear`만 주기적으로 호출한다.

### Why
- 현재 코드와 테스트 변경 폭을 가장 작게 유지하면서도 대량 처리 시 1차 캐시 누적을 줄일 수 있다.
- 부분 저장과 실패 행 수집이라는 기존 서비스 의도를 크게 흔들지 않는다.
- JDBC batch 설정은 성능 최적화 효과는 있지만, 이번 작업 목표인 처리 방식 단순화와는 맞지 않았다.
- 운영 환경별 업로드 정책을 코드 수정 없이 조정할 수 있다.

## 2026-04-10 Query flat 구조와 grouped service 기준

### Decision
- `command`는 기존처럼 `application/domain/infrastructure` 레이어 구조를 유지한다.
- `query`는 `application/infrastructure`를 제거하고 `controller/service/dto/mapper` flat 구조로 정리한다.
- Query DTO는 명확한 입출력 DTO만 `dto/request`, `dto/response`로 이동하고, 애매한 DTO는 별도 위치에 유지한다.
- grouped controller에 맞춰 query service도 `SellerOrderQueryService`, `AdminOrderQueryService`, `WhmOrderQueryService`, `OrderDashboardQueryService`로 묶는다.
  - `SellerOrderQueryService`: 목록 + 상세 + 상태 이력
  - `AdminOrderQueryService`: 관리자 목록 + 주문 상세
  - `WhmOrderQueryService`: 창고 목록 + 송장 CSV
  - `OrderDashboardQueryService`: 출고 통계 + KPI
- bulk 업로드는 `BulkOrderCommandService` 하나로 create/validate/템플릿 헤더 정책을 묶고, `OrderIdGenerator`는 독립 지원 컴포넌트로 유지한다.

### Context
- 이전 리팩터링으로 command/query 모두 `application/infrastructure`를 도입했지만, query 쪽은 조회 조립 중심이라 경로만 길어지고 기능 맥락 파악이 느려졌다.
- controller는 이미 Actor·URL 기준으로 묶인 상태였는데 service는 여전히 use case 단위로 흩어져 있어 구조 대칭이 맞지 않았다.
- bulk create와 validate는 같은 파일 구조, 같은 검증 규칙, 같은 템플릿 헤더를 공유하고 있었다.

### Alternatives Considered
- command/query 모두 레이어 구조를 그대로 유지한다.
- query도 Actor별 feature package로 더 쪼갠다.
- bulk create와 validate를 계속 별도 서비스로 유지한다.

### Why
- query는 도메인 규칙보다 조회 조립과 mapper 호출이 중심이라 flat 구조가 탐색 비용을 줄인다.
- grouped controller와 grouped service를 맞추면 의존성 그래프와 테스트 구성이 단순해진다.
- bulk create/validate는 입력 포맷과 정책이 같아 하나의 서비스로 묶는 편이 하드코딩 제거와 정책 일관성 측면에서 유리하다.
- 반면 `OrderIdGenerator`는 유스케이스가 아니라 지원 컴포넌트이므로 독립 유지가 맞다.
