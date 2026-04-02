# team1-backend-order

Order 도메인 백엔드 저장소다. 문서와 구현은 `docs/CONK_API_명세서 (2).xlsx`의 `ORDER` 시트를 기준으로 맞춘다.

## 현재 구현 상태

- 구현됨
  - 주문 도메인 aggregate `Order`, `OrderItem`, `ShippingAddress`, `OrderStatus`
  - JPA 기반 `OrderRepository`
  - ORD-001 `GET /orders/outbound/stats` 조회 API
- 진행중
  - ORD-001의 `trend`, `trendLabel`, `trendType`은 현재 고정값으로 반환한다.
  - 명세 기준 인증은 `bearer`지만 현재 저장소에는 Spring Security/JWT 설정이 없다.
- 대기
  - ORD-002 ~ ORD-009

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
- MariaDB Driver
- H2 Test DB
- JUnit 5 / Spring Boot Test

## 현재 패키지 구조

```text
src/main/java/com/conk/order
├── command
│   └── domain
│       ├── aggregate
│       └── repository
└── query
    ├── controller
    ├── dto
    ├── mapper
    └── service
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
- [TDD 가이드](docs/TDD.md)
