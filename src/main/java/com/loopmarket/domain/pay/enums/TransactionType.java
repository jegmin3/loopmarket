package com.loopmarket.domain.pay.enums;

/**
 * 거래 유형
 */
public enum TransactionType {
    CHARGE,       // 자체페이 충전
    REFUND,       // 자체페이 환불
    BUY_NOW,      // 대면 거래 즉시결제
    SAFE_PAY,     // 비대면/택배 거래 안전결제 (잔액 보류)
    SETTLEMENT    // 구매 확정 후 판매자 정산 처리
}