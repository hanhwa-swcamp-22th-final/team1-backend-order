package com.conk.order.command.infrastructure.repository;

import com.conk.order.command.domain.aggregate.OrderIdSequence;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

/*
 * 날짜별 주문 ID 시퀀스 저장소.
 * 동시 주문 등록 시 중복 시퀀스 발급을 막기 위해 PESSIMISTIC_WRITE 락을 사용한다.
 */
public interface OrderIdSequenceRepository extends JpaRepository<OrderIdSequence, LocalDate> {

  /** 해당 날짜의 시퀀스 행을 배타 락으로 조회한다. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT s FROM OrderIdSequence s WHERE s.seqDate = :date")
  Optional<OrderIdSequence> findBySeqDateForUpdate(LocalDate date);
}
