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
./gradlew test --tests "com.conk.order.command.domain.aggregate.OrderTest"  # 클래스 단위
./gradlew test --tests "com.conk.order.command.domain.aggregate.OrderTest.createCreatesPendingOutboundOrder"  # 메서드 단위
./gradlew bootRun                       # 애플리케이션 실행 (application-dev.yml 필요)
```

`application-dev.yml`은 `.gitignore`에 포함되어 있으므로 로컬에서 직접 생성해야 한다. DB 접속 정보(`spring.datasource`)와 `spring.jpa.hibernate.ddl-auto` 설정이 필요하다.

## 아키텍처

### CQRS 패키지 분리

```
command/
  domain/aggregate/     Order, OrderItem, ShippingAddress, OrderStatus (JPA 엔티티)
  domain/repository/    OrderRepository (Spring Data JPA)
query/
  controller/           REST 컨트롤러
  service/              비즈니스 로직 (MyBatis Mapper 호출)
  mapper/               MyBatis @Mapper 인터페이스
  dto/                  ApiResponse<T>, 응답 DTO
```

**쓰기(Command)**: JPA (`OrderRepository`)
**읽기(Query)**: MyBatis (`OutboundStatsQueryMapper` + XML)

### 응답 래퍼

- 조회 API: `ApiResponse<T>` → `{ success: true, data: {...} }`
- 생성 API (미구현): `{ success: true, message: "...", data: {...} }`

API 명세 base path: `/orders`, 인증: `bearer` (현재 Security 미구현)

### MyBatis XML mapper

`src/main/resources/mappers/*.xml` — `classpath:mappers/*.xml`로 설정됨 (`application.yml`에 공통 등록).

## 개발 워크플로우

### 브랜치

`feat/...`, `fix/...`, `test/...`, `docs/...` 형식. `main`에서 직접 작업 금지.

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
| Domain | JUnit 5 (순수 자바) | 생성 규칙, 상태 전이, 검증 로직 전수 |
| Repository | `@DataJpaTest` + H2 | 커스텀 쿼리 메서드만, `save()`/`findById()` 생략 |
| Service (Query) | JUnit 5 + Fake Mapper | Mapper를 직접 구현한 Stub 사용, Spring 컨텍스트 없이 단위 테스트 |
| Controller | 미구현 |  |

Service 테스트는 Mockito 대신 **직접 구현한 Stub** 클래스를 사용한다 (`FixedOutboundStatsQueryMapper` 패턴 참고).

## 현재 구현 상태

- **완료**: 도메인 aggregate, `OrderRepository`, `GET /orders/outbound/stats` (ORD-001) 기본 응답
- **진행중 (feat/order-outbound-stats)**: ORD-001 `trend`/`trendLabel`/`trendType` 추이 계산 로직 — 현재 고정값 반환 중
- **대기**: ORD-002 ~ ORD-009

## 주요 문서 위치

- API 계획 및 기능 상태: `docs/order-development-plan.md`
- TDD 가이드: `docs/TDD.md`
- 트러블슈팅: `docs/troubleshooting.md`
- 개발 로그: `docs/order-dev-log.md`
