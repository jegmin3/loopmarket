package com.loopmarket.domain.pay.service;

import com.loopmarket.domain.pay.enums.PaymentMethod;

/**
 * 페이 관련 기능(충전, 환불 등)을 정의한 서비스 인터페이스
 */
public interface PayService {

    // 외부 결제 성공 후 자체페이에 금액 충전
    void charge(Long userId, int amount, PaymentMethod method);

    // 잔액에서 환불 처리 (내부 차감)
    void refund(Long userId, int amount);
    
    // 현재 유저의 잔액 조회 (존재하지 않으면 0 반환)
    int getBalance(Long userId);
    
    // 안전결제 처리 로직 (구매자 잔액 차감, 거래기록 생성, 상태 변경)
    Long safePay(Long buyerId, Long sellerId, Long productId, int amount);
    
    // 구매 확정 처리 (결제 상태 변경, 판매자 잔액에 금액 정산)
    int completePay(Long paymentId);
}
