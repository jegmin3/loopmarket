package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 페이 환불 API 응답 DTO
 * PayApiController 의 /api/pay/refund API에서 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private boolean success;
    private String message;
    private int balance;
}