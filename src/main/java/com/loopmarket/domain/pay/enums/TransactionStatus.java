package com.loopmarket.domain.pay.enums;

/**
 * 거래 처리 상태
 */
public enum TransactionStatus {
    WAITING,    // 처리 대기
    SUCCESS,    // 처리 완료
    FAIL        // 처리 실패 (잔액 부족, 예외 발생 등)
}