# Project Help

빠른 진입은 아래 문서를 우선 본다.

- `README.md`: 현재 구현 범위와 실행 방법
- `docs/order-development-plan.md`: ORDER API 명세 기준 개발 상태
- `docs/order-dev-log.md`: 날짜별 작업 이력
- `docs/troubleshooting.md`: 이슈 원인과 해결

## Quick Commands

```bash
./gradlew test
./gradlew bootRun
```

## Local Setup Notes

- Java 21 기준이다.
- 기본 활성 프로파일은 `dev`다.
- 실행용 `src/main/resources/application-dev.yml`은 로컬에만 둔다.
- ORDER API Base path는 `/orders`다.

## Reference

- Spring Boot Gradle Plugin
  - https://docs.spring.io/spring-boot/3.5.12/gradle-plugin
- Spring Data JPA
  - https://docs.spring.io/spring-boot/3.5.12/reference/data/sql.html#data.sql.jpa-and-spring-data
- MyBatis Spring Boot Starter
  - https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/
