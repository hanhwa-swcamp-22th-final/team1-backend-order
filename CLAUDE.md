# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 기본 원칙

- **학습 개발이 목적이므로**, 직접 구현 요청이 없는 경우에는 코드 수정이나 파일 생성을 하지 않는다. 구현 방향, 테스트 포인트, 설계 검토, 코드 리뷰 중심으로 지원한다.
- 모든 기능 개발은 TDD Red-Green-Refactor 사이클을 기본으로 한다.
- 개발 순서는 **Inside-Out**: Domain → Repository → Service → Controller.
- 모든 코드 주석은 **한국어**로 작성한다.

## 커맨드

```bash
./gradlew test                          # 전체 테스트
./gradlew test --tests "com.conk.order.command.domain.aggregate.OrderTest"  # domain 단위
./gradlew test --tests "com.conk.order.command.application.service.CreateOrderServiceTest"  # service 단위
./gradlew test --tests "com.conk.order.query.application.controller.OrderKpiQueryControllerTest"  # controller 단위
./gradlew bootRun                       # 애플리케이션 실행 (application-dev.yml 필요)
```

`application-dev.yml`은 `.gitignore`에 포함되어 있으므로 로컬에서 직접 생성해야 한다. DB 접속 정보(`spring.datasource`)와 `spring.jpa.hibernate.ddl-auto` 설정이 필요하다.

## 아키텍처

### CQRS + Hexagonal 패키지 구조

```
common/
  controller/             GlobalExceptionHandler
  dto/                    ApiResponse<T>
  exception/              ErrorCode, BusinessException

command/
  application/
    controller/           CreateOrderController, BulkCreateOrderController
    service/              CreateOrderService, BulkCreateOrderService
    dto/                  Request/Response DTO (6개)
  domain/
    aggregate/            Order, OrderItem, ShippingAddress, OrderStatus, OrderChannel (JPA 엔티티)
    repository/           OrderRepository (Spring Data JPA)
  port/
    OrderSavePort         서비스 → 레포지토리 최소 포트 인터페이스

query/
  application/
    controller/           5개 Query Controller (REST 엔드포인트)
    service/              5개 Query Service (Mapper 호출 + 응답 조립)
    dto/                  Query/Response/Summary DTO (12개)
  infrastructure/
    mapper/               5개 MyBatis @Mapper 인터페이스
```

**쓰기(Command)**: JPA (`OrderRepository`, `OrderSavePort`)
**읽기(Query)**: MyBatis (`*QueryMapper` + XML)

### 응답 래퍼

- 조회 API: `ApiResponse<T>` → `{ success: true, data: {...} }`
- 생성 API: `ApiResponse<T>` → `{ success: true, message: "주문이 등록되었습니다", data: {...} }`

API 명세 base path: `/orders`, 인증: `bearer` (현재 Security 미구현)

### MyBatis XML mapper

`src/main/resources/mappers/*.xml` — `classpath:mappers/*.xml`로 설정됨 (`application.yml`에 공통 등록).

namespace는 `com.conk.order.query.infrastructure.mapper.*` 패턴.

## 개발 워크플로우

### 브랜치

`feat/...`, `fix/...`, `test/...`, `docs/...`, `refactor/...` 형식. `main`에서 직접 작업 금지.

### 커밋

Conventional Commits 형식, 제목 50자 이내, 본문은 "무엇을"보다 "왜"를 적는다.

### 기능 개발 시 문서 업데이트 (필수)

1. `docs/order-development-plan.md` — 기능 상태를 `대기 → 진행중 → 완료`로 갱신, 시작일/완료일/브랜치 기록
2. `docs/order-dev-log.md` — 날짜별 개발 로그 누적 기록 (현재단계, 구현 내용, 마지막 커밋)
3. `docs/troubleshooting.md` — 오류 분석·디버깅 처리 시 원인/해결 기록

### PR

`.github/pull_request_template.md` 형식을 따른다.

## 테스트 전략

| 계층 | 도구 | 범위 |
|---|---|---|
| Domain | JUnit 5 (순수 자바) | 생성 규칙, 상태 전이, 검증 로직 |
| Repository | `@DataJpaTest` + H2 | aggregate 저장/조회, 커스텀 쿼리 |
| Service (Command) | JUnit 5 + Stub Port | port 인터페이스 기반 단위 테스트 |
| Service (Query) | JUnit 5 + Stub Mapper | Mapper 직접 구현 Stub, Spring 컨텍스트 없이 |
| Controller | `@WebMvcTest` + MockBean | HTTP 요청/응답, 상태 코드, 필수값 검증 |
| 통합 | `@SpringBootTest` + H2 | 전체 스택 end-to-end |

## 현재 구현 상태

- **완료**: ORD-001 ~ ORD-007 (출고 통계, 단건/일괄 등록, 셀러/관리자/창고 목록, KPI)
- **대기**: ORD-008 (당월 매출), ORD-009 (월별 매출 추이)
- **미구현**: Spring Security / JWT 인증

전체 테스트 79개, 전부 GREEN.

## 주요 문서 위치

- API 계획 및 기능 상태: `docs/order-development-plan.md`
- TDD 가이드: `docs/ORDER_TDD_GUIDE.md`
- 트러블슈팅: `docs/troubleshooting.md`
- 개발 로그: `docs/order-dev-log.md`
- 설계 결정: `docs/decisions.md`
