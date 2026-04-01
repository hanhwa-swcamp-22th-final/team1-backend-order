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
