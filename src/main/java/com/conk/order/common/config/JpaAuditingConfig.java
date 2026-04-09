package com.conk.order.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/*
 * JPA Auditing 설정.
 *
 * @EnableJpaAuditing 을 활성화하면
 * @CreatedDate, @LastModifiedDate 어노테이션이 엔티티에서 동작한다.
 * 엔티티마다 LocalDateTime.now() 를 직접 호출하지 않아도
 * 영속화(save) 시 자동으로 시간이 채워진다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
