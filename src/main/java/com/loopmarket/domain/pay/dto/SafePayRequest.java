package com.loopmarket.domain.pay.dto;

import lombok.*;

/**
 * 안전결제 요청 API의 요청 DTO
 * PayApiController 의 /api/pay/safe API에서 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafePayRequest {
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private int amount;
}