package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 페이 환불 요청 DTO
 * 프론트엔드에서 전송하는 JSON 데이터를 매핑합니다.
 * PayApiController 의 /api/pay/refund API에서 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
public class RefundRequest {
    
    private Long userId;
    private int amount;

    public RefundRequest(Long userId, int amount) {
        this.userId = userId;
        this.amount = amount;
    }
}