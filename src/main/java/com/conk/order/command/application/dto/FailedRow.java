package com.conk.order.command.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * 엑셀 일괄 주문 등록 시 처리 실패한 행 정보.
 *
 * rowNumber 는 헤더를 1행으로 볼 때의 데이터 행 번호 (2부터 시작).
 * reason 은 실패 원인 메시지 (검증 오류, 중복 주문번호 등).
 */
@Getter
@RequiredArgsConstructor
public class FailedRow {

  /** 엑셀 행 번호 (헤더 = 1행 기준, 데이터는 2행부터). */
  private final int rowNumber;

  /** 실패 원인. */
  private final String reason;
}
