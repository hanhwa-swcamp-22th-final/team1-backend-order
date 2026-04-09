# Order 기능 개발 계획

## 1. 계획

### (1) 문서 목적

- 이 문서는 `CONK_API_명세서 (3).xlsx`의 `ORDER` 시트를 기준으로 Order 백엔드 구현 범위를 정리한다.
- 기능 개발 시작 전 범위, 선행 작업, 테스트 관점을 먼저 정리하고 기능 완료 시 상태를 갱신한다.

### (2) 문서 업데이트 규칙

- 상태는 `대기`, `진행중`, `완료`, `보류`, `이관` 중 하나로 기록한다.
- 기능 개발 시작 시 `상태`, `시작일`, `작업 브랜치`를 갱신한다.
- 기능 개발 완료 시 `상태`, `완료일`, `테스트`, `마지막 커밋`을 갱신한다.
- 기능 범위가 바뀌면 API 명세서와 실제 구현 차이를 `비고`에 남긴다.

### (3) 공통 세부 사항

- 기준 Base path: `/orders`
- 서버 포트: `7001` (application.yml 확정)
- 인증 방식: NGINX가 JWT 검증 후 `X-User-Id` 헤더 주입 → Order 서비스는 헤더에서 추출 (Spring Security 불필요)
- 응답 규칙: 기본 조회는 `success/data`, 생성 계열은 `success/message/data`
- 날짜 포맷: `YYYY-MM-DD`, `YYYY-MM`, `ISO datetime` 혼합
- 기능 테스트 전 엔티티, enum, 연관관계를 먼저 정리하는 Phase 0를 선행한다.
- `sellerId`, `warehouseId`, `invoiceNo`, `sku` 같은 참조 식별자는 모두 `String` 기준으로 잡는다.
- `Seller`, `Warehouse`, `ShipmentInvoice`, `Product`는 소유 도메인 확인 전까지 로컬 엔티티로 확정하지 않고 참조 값으로 우선 다룬다.
- 물리 테이블명은 DB 소유권과 연동 구조 확인 후 최종 확정한다.
- 기본 개발 흐름: 브랜치 생성 -> 테스트 시나리오 작성 -> 테스트 작성 -> 구현 -> 문서 업데이트 -> 커밋

## 2. 기능 묶음별 계획

### (1) Seller 주문 등록

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-002 | POST | /orders/seller/manual | 셀러 단건 주문 등록 | 완료 | 2026-04-03 | 2026-04-03 | feat/order-create-manual | 전체 39개 GREEN |
| ORD-003 | POST | /orders/seller/bulk | 셀러 엑셀 업로드 주문 일괄 등록 | 완료 | 2026-04-05 | 2026-04-05 | feat/order-whm-list | Apache POI 5.3.0. 부분 저장 정책. Service 4, Controller 4 GREEN |
| ORD-011 | GET | /orders/seller/bulk/template | 주문 업로드용 엑셀 템플릿 다운로드 | 대기 | - | - | - | application/octet-stream. ORD-003 참고용 템플릿 |
| ORD-013 | POST | /orders/seller/bulk/validate | 주문 일괄 업로드 사전 검증 | 대기 | - | - | - | ORD-003 등록 전 사전 검증용. multipart/form-data |

### (2) Seller 주문 조회 / 상세 / 취소

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-004 | GET | /orders/seller/list | 셀러 주문 목록 조회 | 완료 | 2026-04-04 | 2026-04-04 | feat/order-seller-list | Service 3, Controller 2 GREEN |
| ORD-008 | GET | /orders/seller/{orderId} | 셀러 주문 상세 조회 | 대기 | - | - | - | canCancel 포함. seller/product 조인 필요. 404/타셀러 차단 |
| ORD-009 | GET | /orders/seller/{orderId}/tracking | 셀러 주문 상태 트래킹 | 대기 | - | - | - | 상태 변경 히스토리 테이블 필요 여부 검토 |
| ORD-010 | PATCH | /orders/seller/{orderId}/cancel | 셀러 주문 취소 | 대기 | - | - | - | RECEIVED/ALLOCATED만 취소 가능. Order.cancelOrder() 재활용 |

### (3) 관리자 및 창고 주문 조회

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-005 | GET | /orders/list | 주문 목록 조회 (masterAdmin) | 완료 | 2026-04-05 | 2026-04-05 | feat/order-admin-list | Service 3, Controller 2, 통합 3 GREEN |
| ORD-006 | GET | /orders/kpi | 주문 KPI 집계 (masterAdmin) | 완료 | 2026-04-05 | 2026-04-05 | feat/order-kpi | Service 2, Controller 2, 통합 3 GREEN |
| ORD-007 | GET | /orders/whm | 창고 관리자 주문 목록 조회 | 완료 | 2026-04-05 | 2026-04-05 | feat/order-whm-list | Service 3, Controller 2, 통합 3 GREEN |
| ORD-014 | GET | /orders/{orderId} | 공통 주문 상세 조회 (관리자/창고/셀러 공용) | 대기 | - | - | - | WMS에서도 호출. seller/product/warehouse 조인 필요 |

### (4) 대시보드 통계

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-001 | GET | /orders/outbound/stats | 대시보드용 출고 통계 조회 | 완료 | 2026-03-31 | 2026-04-03 | feat/order-outbound-stats | 전체 35개 GREEN |

### (5) 파일 다운로드 / CSV

| API ID | Method | Path | 요약 | 상태 | 시작일 | 완료일 | 작업 브랜치 | 비고 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| ORD-012 | GET | /orders/shipments/export | 비연동 채널 송장 CSV 다운로드 | 대기 | - | - | - | 관리자용. Content-Type text/csv |

### (6) 리팩터링 / 개선 작업

| 작업 ID | 요약 | 상태 | 시작일 | 완료일 | 비고 |
| --- | --- | --- | --- | --- | --- |
| REF-001 | CQRS 디렉토리 구조 개선 | 완료 | 2026-04-06 | 2026-04-06 | command/query application/infrastructure 레이어 도입 |
| REF-002 | 서버 포트 7001 확정 | 완료 | 2026-04-09 | 2026-04-09 | application.yml |
| REF-003 | JaCoCo 불필요 의존성 제거 | 완료 | 2026-04-09 | 2026-04-09 | implementation 삭제 |
| REF-004 | JPA Auditing 도입 | 완료 | 2026-04-09 | 2026-04-09 | @CreatedDate/@LastModifiedDate 자동화 |
| REF-005 | sellerId → X-User-Id 헤더 변경 | 대기 | - | - | 3개 컨트롤러 + DTO + 테스트 변경 |
| REF-006 | Workbook 리소스 누수 해결 | 대기 | - | - | try-with-resources 리팩터링 |
| REF-007 | SellerOrderList 통합 테스트 추가 | 대기 | - | - | Seller만 IntegrationTest 누락 |
| REF-008 | BulkCreate 엣지 케이스 테스트 | 대기 | - | - | null cell, FORMULA 셀, 빈 행 등 |
| REF-009 | BulkCreate 채널 MANUAL → EXCEL | 대기 | - | - | OrderChannel.EXCEL 미사용 |
| REF-010 | Query DTO 공통 추상 클래스 도입 | 대기 | - | - | PageableOrderListQuery 추출 |
| REF-011 | BulkCreate flush/clear 기반 메모리 관리 | 완료 | 2026-04-09 | 2026-04-09 | JDBC batch 설정 제거, 업로드 제한값 설정 분리, 관련 테스트 GREEN |

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
