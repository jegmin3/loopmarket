package com.loopmarket.domain.pay.dto;

import lombok.Getter;

/**
 * 구매 확정 요청 DTO
 * 프론트에서 paymentId만 전달받아 처리
 */
@Getter
public class CompletePayRequest {
    private Long paymentId;
    private Long buyerId;
}