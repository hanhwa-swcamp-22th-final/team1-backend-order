# Order 트러블슈팅

## 기록 규칙

- 디버깅이나 오류 분석 요청을 처리할 때마다 항목을 누적 작성한다.
- 핵심은 `원인`과 `해결`이 바로 보이게 적는 것이다.
- 필요하면 영향 범위, 재현 방법, 검증 결과까지 함께 적는다.
- 항목은 최신 이슈를 아래에 추가하는 방식으로 관리한다.

## 작성 템플릿

### [이슈 제목]

**Problem**  
- 어떤 문제가 발생했는지 적는다.

**Impact**  
- 개발이나 실행에 어떤 영향이 있었는지 적는다.

**Reproduction**  
- 재현 조건이나 재현 순서를 적는다.

**Cause**  
- 확인된 원인을 적는다.

**Fix**  
- 적용한 해결 방법을 적는다.

**Verification**  
- 해결 후 무엇으로 확인했는지 적는다.

**Prevention**  
- 다음에 같은 문제를 줄이기 위한 체크 포인트를 적는다.

**Related**  
- 관련 파일이나 브랜치를 적는다.

## 2026-03-27

### [ORD-001 query 계층 컴파일 오류]

**Problem**  
- `OutboundStatsQueryDao`, `OutboundStatsQueryService`, `OutboundStatsResponse`, `OutboundStatsQueryServiceTest` 작성 중 빨간줄이 다수 발생했다.

**Cause**  
- DAO를 `class`로 선언하고 메서드 본문 없이 작성했다.
- Service에 `outboundStatsQueryDao` 필드/생성자가 없었다.
- DTO에 생성자와 getter가 없었다.
- 테스트에서 오타, 잘못된 타입명, 중복 메서드명, 잘못된 assertion 문법이 섞였다.

**Fix**  
- DAO는 `interface`로 선언하거나 메서드 본문을 구현한다.
- Service에 DAO 의존성을 필드/생성자로 추가한다.
- DTO에 필요한 생성자와 getter를 추가한다.
- 테스트의 메서드명/타입명/Mockito 문법/assertion 오타를 정리한다.
