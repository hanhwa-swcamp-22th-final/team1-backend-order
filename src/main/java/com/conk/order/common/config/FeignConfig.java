package com.conk.order.common.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 내부 서비스 호출용 Feign 공통 헤더를 주입한다.
 */
@Configuration
public class FeignConfig {

    private static final String HEADER_INTERNAL_CALL = "X-Internal-Call";

    @Bean
    public RequestInterceptor internalCallRequestInterceptor() {
        return template -> template.header(HEADER_INTERNAL_CALL, "true");
    }
}
