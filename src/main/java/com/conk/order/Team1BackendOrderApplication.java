package com.conk.order;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class Team1BackendOrderApplication {

  public static void main(String[] args) {
    SpringApplication.run(Team1BackendOrderApplication.class, args);
  }

  /* 시스템 기본 시간대를 사용하는 Clock 빈을 등록한다. */
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
