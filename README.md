# team1-backend-order

Order 도메인 백엔드 저장소다. 문서와 구현은 `docs/CONK_API_명세서 (2).xlsx`의 `ORDER` 시트를 기준으로 맞춘다.

## 현재 구현 상태

- 완료
  - 주문 도메인 aggregate `Order`, `OrderItem`, `ShippingAddress`, `OrderStatus`, `OrderChannel`
  - JPA 기반 `OrderRepository`
  - ORD-001 `GET /orders/outbound/stats` 대시보드 출고 통계 조회
  - ORD-002 `POST /orders/seller/manual` 셀러 단건 주문 등록
  - ORD-003 `POST /orders/seller/bulk` 셀러 엑셀 일괄 주문 등록
  - ORD-004 `GET /orders/seller/list` 셀러 주문 목록 조회
  - ORD-005 `GET /orders/list` 관리자 주문 목록 조회
  - ORD-006 `GET /orders/kpi` 주문 KPI 집계
  - ORD-007 `GET /orders/whm` 창고 관리자 주문 목록 조회
- 대기
  - ORD-008 `GET /orders/revenue/current` 당월 매출 조회
  - ORD-009 `GET /orders/revenue/monthly` 월별 매출 추이 조회
  - Spring Security / JWT 인증 연동

## 명세 기준

- Base path: `/orders`
- 인증: `bearer`
- 응답 래퍼
  - 조회: `success/data`
  - 생성: `success/message/data`

## 기술 스택

- Java 21
- Spring Boot 3.5.12
- Spring Web
- Spring Data JPA
- MyBatis
- Spring Validation
- Apache POI 5.3.0 (엑셀 업로드)
- MariaDB Driver
- H2 Test DB
- JUnit 5 / Spring Boot Test

## 패키지 구조

```text
src/main/java/com/conk/order
├── common
│   ├── controller          GlobalExceptionHandler
│   ├── dto                 ApiResponse
│   └── exception           ErrorCode, BusinessException
├── command
│   ├── application
│   │   ├── controller      CreateOrderController, BulkCreateOrderController
│   │   ├── service         CreateOrderService, BulkCreateOrderService
│   │   └── dto             Request/Response DTO
│   ├── domain
│   │   ├── aggregate       Order, OrderItem, ShippingAddress, OrderStatus, OrderChannel
│   │   └── repository      OrderRepository
│   └── port                OrderSavePort
└── query
    ├── application
    │   ├── controller      5개 Query Controller
    │   ├── service         5개 Query Service
    │   └── dto             Query/Response/Summary DTO
    └── infrastructure
        └── mapper          5개 MyBatis Mapper
```

## 실행 및 테스트

애플리케이션 실행 시 `src/main/resources/application-dev.yml`이 필요하다. 이 파일은 로컬 환경 설정용이라 저장소에 포함하지 않는다.

```bash
./gradlew test
./gradlew bootRun
```

## 문서

- [개발 계획](docs/order-development-plan.md)
- [개발 로그](docs/order-dev-log.md)
- [트러블슈팅](docs/troubleshooting.md)
- [검토 기준](docs/skill.md)
- [TDD 가이드](docs/ORDER_TDD_GUIDE.md)
- [설계 결정](docs/decisions.md)
