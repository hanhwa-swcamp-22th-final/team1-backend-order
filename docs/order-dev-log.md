# Order 개발로그

## 기록 규칙

- 날짜별로 로그를 누적 작성한다.
- 기능 개발을 시작하면 구현 전 상태와 목표를 먼저 적는다.
- 기능 개발이 끝나면 같은 항목에 테스트 결과와 마지막 커밋 내용을 추가한다.
- 개발로그 내용은 `docs/order-development-plan.md`의 상태 갱신 내용과 일치해야 한다.

## 작성 템플릿

### 날짜 섹션 템플릿

## YYYY-MM-DD

### 현재단계 :
- 현재 구현 전 상태를 적는다.
- 어떤 범위를 이번 작업에서 다루는지 적는다.

### 작업 브랜치 :
- `feat/...`

### 구현 시 바뀌는 점 :
- 구현 후 사용자나 호출자가 체감하는 변경점을 적는다.

### 추가하는 코드
- `src/...`
    - 추가하거나 수정하는 책임을 적는다.

### 테스트
- 작성한 테스트와 확인 결과를 적는다.

### 확인해야할 점 :
- 리뷰 포인트, 후속 확인 사항, 남은 리스크를 적는다.

### 마지막 커밋 내용 :
- `feat: example`

## 2026-03-27

### 현재단계 :
- Order 백엔드 기능 구현 전 공통 개발 원칙과 문서 운영 규칙이 없는 상태였다.
- 기능 시작 및 완료 시 어떤 문서를 갱신해야 하는지 기준이 없었다.

### 작업 브랜치 :
- `dev`

### 구현 시 바뀌는 점 :
- Order 기능 개발 전에 참고할 계획 문서와 날짜별 개발로그 기준이 생긴다.
- 기능 시작 시 계획 문서와 개발로그를 함께 갱신하는 흐름이 정리된다.
- 기능 완료 시 마지막 커밋 내용을 개발로그에 남기는 기준이 생긴다.
- 디버깅 결과를 누적할 트러블슈팅 문서 기준이 추가된다.

### 추가하는 코드
- `AGENTS.md`
    - CONK 백엔드 Order 저장소 작업 원칙과 문서 운영 규칙 추가
- `docs/order-development-plan.md`
    - ORDER API 기준 기능 계획 문서 추가
- `docs/order-dev-log.md`
    - 날짜별 개발로그 템플릿과 초기 기록 추가
- `docs/troubleshooting.md`
    - 트러블슈팅 기록 규칙과 템플릿 추가
- `.github/pull_request_template.md`
    - 저장소에 세팅된 PR 템플릿 기준을 유지하도록 정리

### 테스트
- 문서 파일 추가 작업으로 별도 실행 테스트는 없음

### 확인해야할 점 :
- 기능 개발 시작 시 이 문서의 템플릿을 그대로 복사하지 말고 실제 작업 내용으로 갱신해야 한다.
- 디버깅 요청 처리 시 `docs/troubleshooting.md`도 함께 갱신해야 한다.
- PR 작성 시 저장소에 세팅된 템플릿 항목을 기준으로 작성해야 한다.

### 마지막 커밋 내용 :
- 아직 기록 전

---

### 현재단계 :
- Order 도메인 규칙을 TDD 방식으로 잡기 시작한 상태였다.
- `Order`, `OrderStatus` 최소 도메인과 `OrderTest`를 먼저 작성해 도메인 계층부터 고정하기 시작했다.

### 작업 브랜치 :
- `feat/order-outbound-stats`

### 구현 시 바뀌는 점 :
- 주문 생성 시 기본 상태가 `PENDING_OUTBOUND`로 시작된다는 규칙이 도메인에 반영되기 시작했다.
- 출고 대기 여부 판단, 출고 완료 처리, 취소 주문의 출고 완료 불가 규칙을 테스트로 먼저 확인할 수 있게 됐다.
- 주문번호와 주문일자 필수값 검증이 도메인 레벨에서 잡히기 시작했다.

### 추가하는 코드
- `src/main/java/com/conk/order/command/domain/aggregate/Order.java`
    - 주문 생성, 상태 변경, 필수값 검증을 담는 최소 도메인 객체 작성
- `src/main/java/com/conk/order/command/domain/aggregate/OrderStatus.java`
    - `PENDING_OUTBOUND`, `OUTBOUND_COMPLETED`, `CANCELED` 상태 enum 추가
- `src/test/java/com/conk/order/command/domain/aggregate/OrderTest.java`
    - 주문 생성, 출고 대기 판단, 취소 주문 출고 완료 방지, 필수값 검증 테스트 추가

### 테스트
- `OrderTest` 기준으로 총 5개 시나리오를 작성함
- 테스트 실행 결과는 아직 개발로그에 기록되지 않았음

### 확인해야할 점 :
- `Order` 도메인이 실제로 green 상태인지 테스트 실행 결과를 확인해야 한다.
- `cancel()` 정책을 출고 완료 후에도 허용할지 추가 규칙 결정이 필요하다.
- 다음 단계로 넘어가기 전에 `Order`를 먼저 닫고 `OrderItem`으로 확장하는 흐름이 적절하다.

### 마지막 커밋 내용 :
- 아직 기록 전

## 2026-03-28

### 현재단계 :
- `Order` 도메인의 1차 초안이 생긴 상태에서, 생성/상태 전이/예외 규칙을 더 구체적으로 다듬는 단계였다.
- `cancelOrder()` 정책과 필수값 검증, 예외 메시지 일관성을 보강하면서 `Order`를 먼저 닫는 방향으로 진행했다.

### 작업 브랜치 :
- `feat/order-outbound-stats`

### 구현 시 바뀌는 점 :
- 주문 생성 팩토리 메서드 `create()`와 주문일자 검증이 보강됐다.
- 출고 완료된 주문은 취소할 수 없고, 취소된 주문은 출고 완료 처리할 수 없다는 상태 전이 규칙이 테스트 기준으로 정리됐다.
- `cancel()` 대신 `cancelOrder()`로 의미가 더 명확한 메서드명으로 정리됐다.
- 주문번호/주문일자 필수값 검증과 예외 메시지 정합성을 테스트로 확인하는 흐름이 추가됐다.

### 추가하는 코드
- `src/main/java/com/conk/order/command/domain/aggregate/Order.java`
    - `create()` 팩토리 메서드 추가
    - `validateOrderDate()` 추가
    - `cancelOrder()` 정책 보강
    - 예외 메시지와 상태 전이 규칙 정리
- `src/main/java/com/conk/order/command/domain/aggregate/OrderStatus.java`
    - 주문 상태 enum 유지 및 도메인 정책 기준으로 사용
- `src/test/java/com/conk/order/command/domain/aggregate/OrderTest.java`
    - 주문 생성 기본 상태 테스트 보강
    - 주문번호/주문일자 필수값 예외 테스트 추가
    - 출고 완료 후 주문 취소 불가 테스트 추가

### 테스트
- `OrderTest` 기준으로 주문 생성, 출고 대기 여부, 취소 후 출고 완료 불가, 필수값 검증, 출고 완료 후 취소 불가 시나리오를 보강했다.
- 커밋 로그 기준으로 테스트 코드는 추가됐지만, 실제 실행 결과는 개발로그에 별도로 남아 있지 않다.

### 확인해야할 점 :
- `Order` 테스트가 실제로 전부 green인지 실행 결과를 확인해야 한다.
- `CANCELED` 상태에서 `cancelOrder()`를 다시 호출할 때 정책을 멱등으로 둘지 예외로 막을지 아직 명확히 정하지 않았다.
- 다음 단계로 넘어갈 때는 `OrderItem` 도메인 규칙을 테스트로 먼저 고정하는 흐름이 자연스럽다.

### 마지막 커밋 내용 :
- `test: 주문 도메인 규칙 테스트 추가`

## 2026-03-30

### 현재단계 :
- `application.properties` 기반 단일 설정에서 `application.yml` 기반 설정으로 전환하는 단계였다.
- 이후 Repository 테스트와 환경 분리를 위해 공통 설정과 dev 프로파일 구조를 먼저 정리하는 작업을 진행했다.

### 작업 브랜치 :
- `chore`

### 구현 시 바뀌는 점 :
- 애플리케이션 공통 설정이 yml 형식으로 통일된다.
- dev 환경 분리를 위한 프로파일 기반 설정 흐름을 사용할 수 있게 된다.
- Repository 테스트를 위한 H2 의존성을 사용할 수 있는 기반이 생긴다.
- 민감한 설정 파일이 저장소에 올라가지 않도록 ignore 규칙이 보강된다.

### 추가하는 코드
- `src/main/resources/application.yml`
    - `spring.application.name`, 활성 프로파일, JPA 공통 설정을 yml 형식으로 정리
- `src/main/resources/application.properties`
    - 기존 properties 설정 파일 제거
- `build.gradle`
    - Repository 테스트용 `com.h2database:h2` 의존성 추가
- `.gitignore`
    - `application-dev.yml`과 로컬 문서 경로 ignore 규칙 추가

### 테스트
- 설정 전환 이후 전체 테스트 실행 기준으로 빌드가 가능한 상태를 확인했다.
- 별도 기능 테스트 추가보다는 환경 전환이 이후 테스트 흐름을 막지 않는지 확인하는 단계였다.

### 확인해야할 점 :
- `spring.profiles.active: dev`를 공통 설정에 둘지, 실행 환경에서 주입할지 팀 기준을 다시 정해야 한다.
- `application-dev.yml`은 환경변수 기반으로 유지하고 하드코딩 계정 정보가 없도록 확인해야 한다.
- 다음 단계에서 Repository 테스트용 test yml과 dev yml 역할을 명확히 분리해야 한다.

### 마지막 커밋 내용 :
- `chore: yml 기반 설정 전환 및 dev 프로파일 분리`

---

### 현재단계 :
- `Order`, `OrderItem`, `ShippingAddress` 도메인 초안을 JPA 기준으로 저장/조회 가능한 구조로 연결하는 단계였다.
- `TDD.md` 기준으로 Domain 이후 `Repository 계층` 테스트를 추가해 실제 저장과 상태 기반 조회를 확인하는 작업을 진행했다.

### 작업 브랜치 :
- `feat/order-domain-foundation`

### 구현 시 바뀌는 점 :
- 주문 aggregate를 DB에 저장하고 다시 조회할 수 있는 기반이 생긴다.
- 출고 대기 상태 기준 건수 조회가 가능해져 `ORD-001` 서비스 구현의 하위 기반이 준비된다.
- 도메인 테스트만 있던 상태에서 Repository 테스트까지 추가되어 DB 저장/조회 흐름을 검증할 수 있게 된다.

### 추가하는 코드
- `src/main/java/com/conk/order/command/domain/aggregate/Order.java`
    - JPA 엔티티 기준으로 주문 aggregate 매핑 반영
- `src/main/java/com/conk/order/command/domain/aggregate/OrderItem.java`
    - 주문 항목과 주문 간 연관관계 매핑 반영
- `src/main/java/com/conk/order/command/domain/aggregate/ShippingAddress.java`
    - 배송지 값 객체의 JPA 매핑 반영
- `src/main/java/com/conk/order/command/domain/repository/OrderRepository.java`
    - 주문 저장소 인터페이스 추가
- `src/test/java/com/conk/order/command/domain/repository/OrderRepositoryTest.java`
    - aggregate 저장과 상태 기반 건수 조회 테스트 추가
- `build.gradle`
    - Repository 테스트용 H2 의존성 추가

### 테스트
- `OrderRepositoryTest`
    - 주문 aggregate 저장 후 재조회 시 주문 항목과 배송지 값이 유지되는지 확인
    - `countByStatus(PENDING_OUTBOUND)`가 기대한 건수를 반환하는지 확인
- `./gradlew test` 기준 전체 테스트 통과 상태를 확인했다.

### 확인해야할 점 :
- `ORD-001`은 조회 API이므로 다음 단계에서는 `query` 계층 서비스 테스트로 넘어가야 한다.
- 계획 문서의 `ORD-001` 상태와 실제 시작 브랜치 기록이 아직 맞춰지지 않았는지 다시 확인해야 한다.
- 설정 전환 커밋과 Repository 커밋이 같은 날짜에 있으므로 로그와 마지막 커밋 내용을 기능 단위로 구분해서 유지해야 한다.

### 마지막 커밋 내용 :
- `feat: 주문 레포지토리 계층 추가`

## 2026-04-01

### 현재단계 :
- ORDER API 명세와 현재 저장소 구현 상태를 다시 대조해 문서를 최신화하는 단계였다.
- `ORD-001`은 조회 API 뼈대와 서비스 테스트까지 들어왔지만, 명세 기준으로는 추이 계산과 인증 연동이 아직 남아 있는 상태였다.

### 작업 브랜치 :
- `feat/order-outbound-stats`

### 구현 시 바뀌는 점 :
- 문서 기준 Base path를 명세와 동일한 `/orders`로 통일한다.
- README와 도움말 문서에서 현재 구현 범위, 실행 방법, 명세 대비 미구현 항목이 바로 보이게 된다.
- 개발 계획 문서에 `ORD-001` 진행 상태와 명세 대비 남은 작업을 반영한다.
- 검토 기준 문서가 현재 저장소 구조와 의존성 기준에 맞게 정리된다.
- `docs/*.md`와 `HELP.md`가 Git에서 추적되도록 ignore 규칙을 정리해 문서 변경 이력이 남게 된다.

### 추가하는 코드
- `README.md`
    - 현재 구현 범위, 명세 기준, 실행 방법, 문서 링크 반영
- `HELP.md`
    - 생성형 기본 안내 대신 프로젝트 기준 빠른 참고 문서로 정리
- `docs/order-development-plan.md`
    - `/orders` 기준 통일 및 `ORD-001` 진행 상태 반영
- `docs/order-dev-log.md`
    - 문서 최신화 작업 로그 추가
- `docs/skill.md`
    - 현재 저장소 구조, 기술 스택, 검토 포인트를 실제 코드 기준으로 정리
- `.gitignore`
    - `docs/*.md`, `HELP.md`는 추적 가능하게 두고 민감 설정만 계속 ignore 하도록 조정

### 테스트
- `./gradlew test` 실행 결과 전체 테스트 통과
- 현재 테스트 범위는 도메인, Repository, `OutboundStatsQueryService`, 애플리케이션 컨텍스트 로드까지 포함한다.

### 확인해야할 점 :
- `ORD-001`의 `trend`, `trendLabel`, `trendType`은 명세상 계산 값이지만 현재는 고정값이다.
- 명세는 `sales_order` 계열 테이블을 기준으로 적혀 있고, 현재 구현은 `orders` 엔티티/매퍼를 사용한다.
- 명세상 인증은 `bearer`지만 현재 저장소에는 Spring Security/JWT 의존성과 설정이 없다.

### 마지막 커밋 내용 :
- 아직 기록 전

---

### 현재단계 :
- ORDER 가이드 기준으로 전체 프로젝트 진행 순서를 다시 잠그는 단계였다.
- 기능별 테스트 전에 엔티티, enum, 연관관계를 먼저 정리하고 이후 구현을 그 구조에 맞추기로 결정했다.
- `ORD-001`은 가이드 구조를 따르되 추이는 프로젝트 합의 규칙을 유지하기로 정리했다.

### 작업 브랜치 :
- `feat/order-outbound-stats`

### 구현 시 바뀌는 점 :
- 이후 개발은 `Phase 0 -> Phase 1~4` 순서로 진행한다.
- 상태 모델이 가이드 기준 6단계 + `CANCELED` 로 재정의된다.
- `ORD-001`의 출고 대기 건수는 `RECEIVED`만 집계하는 기준으로 맞춘다.
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 소유 도메인 확인 전까지 참조 값으로만 다루는 방향이 문서에 반영된다.

### 추가하는 코드
- `docs/decisions.md`
    - 엔티티 선도출, 상태 모델, `ORD-001` 예외 규칙, 외부 도메인 후보 엔티티 처리 기준 기록
- `docs/ORDER_TDD_GUIDE.md`
    - Phase 0 추가 및 `ORD-001` 규칙을 현재 프로젝트 합의에 맞게 반영
- `docs/order-development-plan.md`
    - 공통 세부 사항과 `ORD-001` 비고를 새 기준으로 갱신
- `docs/order-dev-log.md`
    - 이번 설계 결정 로그 추가

### 테스트
- 문서 갱신 작업으로 별도 테스트 실행은 없음

### 확인해야할 점 :
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`의 실제 소유 도메인과 조회 방식은 추후 확인이 필요하다.
- 물리 테이블명이 `orders`인지 `sales_order`인지도 DB 소유권 확인 후 확정해야 한다.
- 상태 모델 변경 시 기존 `Order`, `OrderRepositoryTest`, `ORD-001` Mapper/Service 영향 범위를 함께 점검해야 한다.

### 마지막 커밋 내용 :
- 아직 기록 전

## 2026-04-03

### 현재단계 :
- Phase 0 도메인 정비 완료 후 ORD-001 컨트롤러 구현과 통합 테스트를 마무리하는 단계였다.
- trendLabel 명칭 확정, 코드 리뷰 지적 사항 수정, 컨트롤러/통합 테스트 추가로 ORD-001을 완전히 닫는 작업을 진행했다.

### 작업 브랜치 :
- `feat/order-outbound-stats`

### 구현 시 바뀌는 점 :
- `GET /orders/outbound/stats` 가 실제 HTTP 요청을 받아 응답을 반환하는 전체 흐름이 완성됐다.
- trendLabel 이 "전 영업일 대비"로 확정됐다 (월요일 포함 모든 평일 동일 표기).
- 컨트롤러 단위 테스트(`@WebMvcTest`)와 전체 스택 통합 테스트(`@SpringBootTest`)가 모두 추가됐다.
- `cancelOrder()` 취소 가능 범위가 `RECEIVED`, `ALLOCATED` 상태만 허용하도록 수정됐다.
- `Order.orderNo` 필드에 `@Column(name = "order_id")` 가 추가돼 DB 컬럼명 불일치가 해소됐다.

### 추가하는 코드
- `src/main/java/com/conk/order/query/controller/OutboundStatsQueryController.java`
    - `GET /orders/outbound/stats` 엔드포인트 처리, ApiResponse 래핑
- `src/test/java/com/conk/order/query/controller/OutboundStatsQueryControllerTest.java`
    - `@WebMvcTest` 기반 평일 정상 응답 / 주말 null 응답 2개 시나리오
- `src/test/java/com/conk/order/query/controller/OutboundStatsIntegrationTest.java`
    - `@SpringBootTest` 기반 전체 스택 통합 테스트 2개 시나리오

### 테스트
- `OutboundStatsQueryControllerTest` 2개 GREEN
- `OutboundStatsIntegrationTest` 2개 GREEN
- 전체 테스트 35개 GREEN

### 확인해야할 점 :
- Spring Security / JWT 인증은 현재 미구현 상태로, 이후 별도 작업이 필요하다.
- ORD-002 시작 전 Phase 1 도메인 체크 항목(`isCancellable()`, `warehouseId` 등)을 먼저 확인해야 한다.

### 마지막 커밋 내용 :
- `feat: ORD-001 출고 통계 조회 컨트롤러 구현 및 trendLabel 수정`

## 2026-04-03 (2)

### 현재단계 :
- ORD-002 `POST /orders/seller/manual` 셀러 단건 주문 등록 기능 구현.
- TDD Inside-Out: Service Test → Service → Controller Test → Controller 순 진행.

### 작업 브랜치 :
- `feat/order-create-manual`

### 구현 시 바뀌는 점 :
- `POST /orders/seller/manual` 엔드포인트 추가.
- `orderNo` 는 null 이면 UUID 자동 생성, 값 있으면 중복 검증 후 사용.
- 응답 래퍼 `ApiResponse` 에 `message` 필드 추가 (`@JsonInclude(NON_NULL)` 로 조회 응답에는 미포함).
- `command/port/OrderSavePort` 인터페이스 추가 — 서비스가 JpaRepository 전체가 아닌 최소 포트에만 의존.

### 추가하는 코드
- `command/port/OrderSavePort.java` — `save`, `existsById` 최소 포트 인터페이스
- `command/dto/CreateOrderRequest.java` — 요청 DTO (Bean Validation 포함)
- `command/dto/CreateOrderItemRequest.java` — 항목 요청 DTO
- `command/dto/CreateShippingAddressRequest.java` — 배송지 요청 DTO
- `command/dto/CreateOrderResponse.java` — 응답 DTO (orderNo)
- `command/service/CreateOrderService.java` — 주문 등록 서비스
- `command/controller/CreateOrderController.java` — 주문 등록 컨트롤러
- `query/dto/ApiResponse.java` — message 필드 및 `created()` 팩토리 메서드 추가

### 테스트
- `CreateOrderServiceTest` 5개 GREEN (orderNo 자동생성/직접입력, 중복예외, RECEIVED 상태, 항목 수)
- `CreateOrderControllerTest` 3개 GREEN (정상 201, sellerId 누락 400, items 누락 400)
- 전체 39개 GREEN

### 확인해야할 점 :
- `orderChannel` 은 현재 MANUAL 고정. 추후 다른 채널 API 추가 시 분리 고려.
- 통합 테스트 (`@SpringBootTest`) 는 미작성 — 필요 시 추가.

### 마지막 커밋 내용 :
- `feat: ORD-002 셀러 단건 주문 등록 구현`

## 2026-04-04

### 현재단계 :
- ORD-004 `GET /orders/seller/list` 셀러 주문 목록 조회 기능 구현.
- TDD Inside-Out: Service Test (StubMapper) → Service → Controller Test → Controller → XML Mapper 순 진행.

### 작업 브랜치 :
- `feat/order-seller-list`

### 구현 시 바뀌는 점 :
- `GET /orders/seller/list` 엔드포인트 추가.
- `sellerId` 필수, `status/startDate/endDate` 선택 필터 지원.
- `page/size` 기반 페이징 응답 (`totalCount/page/size` 포함).
- MyBatis XML `<sql id="whereClause">` 재사용으로 `findOrders`/`countOrders` 공통 필터 유지.
- `CreateOrderService.create()` 에 `@Transactional` 추가.

### 추가하는 코드
- `query/dto/SellerOrderListQuery.java` — 쿼리 파라미터 DTO (`getOffset()` 포함)
- `query/dto/SellerOrderSummary.java` — 목록 항목 DTO
- `query/dto/SellerOrderListResponse.java` — 페이징 응답 DTO
- `query/mapper/SellerOrderListQueryMapper.java` — MyBatis @Mapper 인터페이스
- `query/service/SellerOrderListQueryService.java` — Mapper 호출 후 응답 조립
- `query/controller/SellerOrderListQueryController.java` — GET 엔드포인트
- `resources/mappers/SellerOrderListQueryMapper.xml` — dynamic SQL (status/날짜 필터, LIMIT/OFFSET 페이징)
- `command/service/CreateOrderService.java` — `@Transactional` 추가

### 테스트
- `SellerOrderListQueryServiceTest` 3개 GREEN (2건 반환, 빈 결과, page/size 반영)
- `SellerOrderListQueryControllerTest` 2개 GREEN (200 정상, sellerId 누락 400)
- 전체 테스트 GREEN

### 확인해야할 점 :
- `ORDER BY ordered_at DESC` 정렬 기준은 요구사항 변경 시 쿼리 파라미터로 분리 고려.
- XML의 `ORDER_CHANNEL` enum 값이 DB 저장 형식과 일치하는지 실 DB 연동 시 확인.
- `@ControllerAdvice` 전역 예외 처리기 미구현 — ORD-005 전에 추가 권장.

### 마지막 커밋 내용 :
- `feat: ORD-004 셀러 주문 목록 조회 구현`
