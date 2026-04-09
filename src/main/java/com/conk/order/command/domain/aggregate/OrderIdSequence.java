package com.conk.order.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/*
 * 날짜별 주문 ID 시퀀스 테이블.
 * 하루에 최대 99999 건의 주문 ID 를 채번한다.
 * 물리 테이블: order_id_sequence
 */
@Entity
@Table(name = "order_id_sequence")
public class OrderIdSequence {

  /** 기준 날짜 (PK). 날짜가 바뀌면 새 행이 생성되고 시퀀스가 1 부터 다시 시작한다. */
  @Id
  @Column(name = "seq_date")
  private LocalDate seqDate;

  /** 해당 날짜의 마지막 시퀀스 번호. */
  @Column(name = "last_seq", nullable = false)
  private int lastSeq;

  protected OrderIdSequence() {}

  /** 날짜 기준으로 첫 번째 시퀀스 행을 생성한다. lastSeq 는 0 으로 초기화한다. */
  public static OrderIdSequence of(LocalDate date) {
    OrderIdSequence seq = new OrderIdSequence();
    seq.seqDate = date;
    seq.lastSeq = 0;
    return seq;
  }

  /**
   * 시퀀스를 1 증가시키고 증가된 값을 반환한다.
   * 99999 를 초과하면 예외를 던진다.
   */
  public int increment() {
    if (lastSeq >= 99999) {
      throw new IllegalStateException("일일 주문 ID 시퀀스가 최대값(99999)에 도달했습니다.");
    }
    return ++lastSeq;
  }
}
