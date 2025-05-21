package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 충전 API 응답 DTO
 * PayApiController 에서 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {
    private boolean success;    // 성공 여부
    private String message;     // 사용자 메시지
    private int balance;        // 충전 후 잔액
}