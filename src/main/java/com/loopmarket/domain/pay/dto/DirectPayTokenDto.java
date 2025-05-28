package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * QR토큰을 담을 전용 DTO 클래스
 */
@Getter
@Setter
@NoArgsConstructor
public class DirectPayTokenDto {

    private Long productId;
    private Long sellerId;
    private String expiresAt;  // ISO 포맷 문자열로 전달됨
}