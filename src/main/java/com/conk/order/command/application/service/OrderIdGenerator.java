package com.conk.order.command.application.service;

import com.conk.order.command.domain.aggregate.OrderIdSequence;
import com.conk.order.command.infrastructure.repository.OrderIdSequenceRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
 * 주문 ID 채번 서비스.
 *
 * 형식: ORD-{년도}-{월일}-{시퀀스 5자리}
 * 예시: ORD-2026-0408-00001
 *
 * 날짜별 시퀀스는 1 부터 시작하고 99999 까지 증가한다.
 * 날짜가 바뀌면 시퀀스가 1 부터 다시 시작한다.
 * 동시성은 DB 비관적 락(PESSIMISTIC_WRITE) 으로 보장한다.
 */
@Service
public class OrderIdGenerator {

  private final OrderIdSequenceRepository sequenceRepository;

  public OrderIdGenerator(OrderIdSequenceRepository sequenceRepository) {
    this.sequenceRepository = sequenceRepository;
  }

  /**
   * 오늘 날짜 기준으로 다음 주문 ID 를 생성한다.
   * 호출 측의 트랜잭션에 합류하므로 별도 트랜잭션을 열지 않는다.
   *
   * @return "ORD-2026-0408-00001" 형식의 주문 ID
   */
  @Transactional
  public String generate() {
    LocalDate today = LocalDate.now();

    OrderIdSequence sequence = sequenceRepository.findBySeqDateForUpdate(today)
        .orElseGet(() -> sequenceRepository.save(OrderIdSequence.of(today)));

    int seq = sequence.increment();

    return String.format("ORD-%d-%02d%02d-%05d",
        today.getYear(),
        today.getMonthValue(),
        today.getDayOfMonth(),
        seq);
  }
}
