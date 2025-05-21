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
    int getCurrentBalance(Long userId);
}
