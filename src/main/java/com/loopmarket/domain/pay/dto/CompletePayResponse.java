package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 구매 확정 응답 DTO
 * PayApiController 의 /api/pay/complete API에서 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class CompletePayResponse {
    private boolean success;
    private String message;
    private int balance; // 정산 후 판매자의 현재 잔액
}