package com.loopmarket.domain.pay.enums;

/**
 * 안전결제 상태를 나타내는 Enum 클래스
 * - HOLD: 결제 금액 보류 상태
 * - COMPLETED: 구매 확정 후 정산 완료
 * - REFUNDED: 환불 완료
 */
public enum PaymentStatus {
    HOLD,
    COMPLETED,
    REFUNDED
}