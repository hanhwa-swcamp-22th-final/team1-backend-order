# Order 기능 개발 계획

## 1. 계획

### (1) 문서 목적

- 이 문서는 `CONK_API_명세서 (2).xlsx`의 `ORDER` 시트를 기준으로 Order 백엔드 구현 범위를 정리한다.
- 기능 개발 시작 전 범위, 선행 작업, 테스트 관점을 먼저 정리하고 기능 완료 시 상태를 갱신한다.

### (2) 문서 업데이트 규칙

- 상태는 `대기`, `진행중`, `완료`, `보류` 중 하나로 기록한다.
- 기능 개발 시작 시 `상태`, `시작일`, `작업 브랜치`를 갱신한다.
- 기능 개발 완료 시 `상태`, `완료일`, `테스트`, `마지막 커밋`을 갱신한다.
- 기능 범위가 바뀌면 API 명세서와 실제 구현 차이를 `비고`에 남긴다.

### (3) 공통 세부 사항

- 기준 Base path: `/orders`
- 인증 방식: `bearer` (명세 기준, 현재 저장소에는 Security/JWT 설정 미구현)
- 응답 규칙: 기본 조회는 `success/data`, 생성 계열은 `success/message/data`
- 날짜 포맷: `YYYY-MM-DD`, `YYYY-MM`, `ISO datetime` 혼합
- 기능 테스트 전 엔티티, enum, 연관관계를 먼저 정리하는 Phase 0를 선행한다.
- `sellerId`, `warehouseId`, `invoiceNo`, `sku` 같은 참조 식별자는 모두 `String` 기준으로 잡는다.
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 소유 도메인 확인 전까지 로컬 엔티티로 확정하지 않고 참조 값으로 우선 다룬다.
- 물리 테이블명은 DB 소유권과 연동 구조 확인 후 최종 확정한다.
- 기본 개발 흐름: 브랜치 생성 -> 테스트 시나리오 작성 -> 테스트 작성 -> 구현 -> 문서 업데이트 -> 커밋

## 2. 기능 묶음별 계획

### (1) Seller 주문 등록

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 테스트 관점 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-002 | POST | /orders/seller/manual | 셀러 단건 주문 등록 | 완료 | 2026-04-03 | 2026-04-03 | feat/order-create-manual | 요청 필수값 검증, 상품 항목 수량 검증, 생성 응답 래퍼 검증 | orderNo 는 null 이면 UUID 자동 생성, 값 있으면 중복 검증. ApiResponse 에 message 필드 추가 (NON_NULL 직렬화). 전체 39개 GREEN |
| ORD-003 | POST | /orders/seller/bulk | 셀러 엑셀 업로드 주문 일괄 등록 | 대기 | - | - | - | 다건 요청 검증, 부분 실패 정책 확인, 생성 응답 래퍼 검증 | 생성 API |

### (2) Seller 주문 조회

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 테스트 관점 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-004 | GET | /orders/seller/list | 셀러 주문 목록 조회 | 완료 | 2026-04-04 | 2026-04-04 | feat/order-seller-list | 필터(status/날짜) null 미적용, 페이징 page/size 반영, 응답 totalCount 검증 | MyBatis XML dynamic SQL (<if> 조건). Service 테스트 3개, Controller 테스트 2개 전체 GREEN |

### (3) 관리자 및 창고 주문 조회

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 테스트 관점 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-005 | GET | /orders/list | 주문 목록 조회 (masterAdmin) | 완료 | 2026-04-05 | 2026-04-05 | feat/order-admin-list | sellerId 선택 필터, 페이징 응답 검증 | ORD-004와 달리 sellerId 선택값. XML &lt;where&gt; 태그로 sellerId null 시 전체 조회. Service 3개, Controller 2개, 통합 3개 GREEN |
| ORD-006 | GET | /orders/kpi | 주문 KPI 집계 (masterAdmin) | 대기 | - | - | - | 집계 기준 기간 검증, KPI 계산 검증 | MasterAdmin 전용 |
| ORD-007 | GET | /orders/whm | 창고 관리자 주문 목록 조회 | 대기 | - | - | - | 창고별 조회 제한, 상태 필터, 페이징 검증 | WHM 전용 |

### (4) 대시보드 통계 및 매출

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 테스트 관점 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-001 | GET | /orders/outbound/stats | 대시보드용 출고 통계 조회 | 완료 | 2026-03-31 | 2026-04-03 | feat/order-outbound-stats | `RECEIVED` 건수 집계, 응답 래퍼, 직전 영업일 대비 추이 계산 검증 | trendLabel은 "전 영업일 대비"로 통일. 컨트롤러·통합 테스트 포함 전체 35개 GREEN |
| ORD-008 | GET | /orders/revenue/current | 대시보드용 당월 총 매출 조회 | 대기 | - | - | - | 월 기준 집계 범위, 금액 계산 검증 | Dashboard 카드 |
| ORD-009 | GET | /orders/revenue/monthly | 월별 매출 추이 조회 (최근 6개월) | 대기 | - | - | - | 최근 6개월 집계, 월 정렬, 누락 월 처리 검증 | Dashboard 차트 |

## 3. 기능 개발 시작 시 체크 항목

### (1) 구현 전 확인

- API 명세서에서 요청/응답 필드를 다시 확인한다.
- 역할별 접근 권한이 Seller, MasterAdmin, WHM 중 어디인지 확인한다.
- 테스트 시나리오를 먼저 작성하고 필요한 fixture를 정리한다.
- 작업 브랜치를 생성하고 해당 브랜치명을 이 문서에 기록한다.

### (2) 구현 완료 후 확인

- 테스트 코드가 기능 요구사항을 모두 덮는지 확인한다.
- 개발로그에 변경점, 테스트 결과, 마지막 커밋 내용을 기록한다.
- 상태를 `완료`로 변경하고 비고에 남은 TODO나 후속 작업을 적는다.
