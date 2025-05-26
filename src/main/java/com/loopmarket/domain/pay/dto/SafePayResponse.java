package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 안전결제 처리 결과 응답 DTO
 * PayApiController의 /api/pay/safe API에서 사용됩니다.
 */
@Getter
@AllArgsConstructor
public class SafePayResponse {
    
    private boolean success;      // 처리 성공 여부
    private String message;       // 사용자 메시지
    private Long paymentId;       // 생성된 결제 ID (보류 상태)
}