# 검토 기준

검토 요청 시 아래 항목을 현재 저장소 기준으로 확인한다.

## 1. 기술 스택

| 항목 | 내용 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5.12 |
| ORM / Query | JPA, MyBatis |
| Validation | spring-boot-starter-validation |
| Runtime DB | MariaDB Driver |
| Test DB | H2 |
| Test | JUnit 5, Spring Boot Test, MyBatis Starter Test |
| 인증 | API 명세는 `bearer`, 현재 저장소에는 Security/JWT 의존성 및 설정 없음 |

## 2. 현재 패키지 구조

```text
command/
  domain/
    aggregate/    -> Order, OrderItem, ShippingAddress, OrderStatus
    repository/   -> OrderRepository

query/
  controller/     -> OutboundStatsQueryController
  dto/            -> ApiResponse, OutboundStatsResponse
  mapper/         -> OutboundStatsQueryMapper, XML 매퍼
  service/        -> OutboundStatsQueryService
```

- `command.application`, `command.infrastructure`, `query.dao` 패키지는 아직 없다.
- 현재 저장소는 도메인/조회 최소 구조만 구현된 상태다.

## 3. API 명세 기준

| 항목 | 내용 |
| --- | --- |
| Base path | `/orders` |
| 조회 응답 | `{ success, data }` |
| 생성 응답 | `{ success, message, data }` |
| 날짜 포맷 | `YYYY-MM-DD`, `YYYY-MM`, `ISO datetime` 혼합 |

## 4. 현재 구현 범위

- 구현됨
  - `Order` aggregate와 상태 전이 규칙
  - `OrderRepository#countByStatus`
  - `ORD-001 GET /orders/outbound/stats`
- 미완료
  - `ORD-001`의 추이 계산 로직
  - Security/JWT 연동
  - `ORD-002` ~ `ORD-009`

## 5. 검토 체크리스트

### (1) 구조 / 설계
- [ ] `command`와 `query` 경계를 넘는 직접 의존이 없는가
- [ ] 조회 로직이 MyBatis Mapper를 통해 분리되어 있는가
- [ ] Controller가 도메인 엔티티를 직접 반환하지 않는가

### (2) 응답 / API
- [ ] Base path가 `/orders` 기준과 일치하는가
- [ ] 조회/생성 응답 래퍼가 명세와 일치하는가
- [ ] `ORD-001`의 추이 값이 고정값인지 실제 계산값인지 구분해서 검토했는가

### (3) 명세 대비 차이
- [ ] 명세 ERD의 `sales_order` 계열과 현재 구현의 `orders` 테이블 사용 차이를 문서나 코드에서 설명하는가
- [ ] 명세상 `bearer` 인증 요구사항과 현재 미구현 상태를 혼동하지 않는가

### (4) 유효성 검증
- [ ] 주문번호, 주문일자, 배송지, 항목, 수량 등 핵심 필수값이 도메인에서 검증되는가
- [ ] 추후 요청 DTO 추가 시 Bean Validation이 빠지지 않도록 체크했는가

### (5) 테스트
- [ ] 도메인 규칙 테스트가 상태 전이와 예외 케이스를 덮는가
- [ ] Repository 테스트가 aggregate 저장과 상태 기반 조회를 확인하는가
- [ ] 조회 서비스 테스트가 응답 조립과 필드 값을 검증하는가
- [ ] 아직 없는 컨트롤러/보안 테스트 공백을 인지하고 있는가

## 6. 개발 흐름 확인

1. `docs/order-development-plan.md`에서 상태와 브랜치를 먼저 갱신한다.
2. 테스트 시나리오를 먼저 정리한다.
3. 테스트 작성 후 최소 구현으로 Green을 만든다.
4. `docs/order-dev-log.md`에 날짜별 변경 이력을 남긴다.
5. 디버깅 이슈가 생기면 `docs/troubleshooting.md`에 원인과 해결만 간단히 누적한다.
