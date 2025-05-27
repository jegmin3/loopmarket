package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 즉시결제 요청을 위한 DTO
 * 구매자 -> 판매자에게 바로 결제를 수행할 때 전달되는 정보
 */
@Getter
@Setter
@NoArgsConstructor
public class DirectPayRequest {

    private Long buyerId;
    private Long sellerId;
    private Long productId;
}