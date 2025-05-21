package com.loopmarket.domain.pay.dto;

import lombok.Data;

/**
 * 페이 충전 요청 DTO
 * 프론트엔드에서 전송하는 JSON 데이터를 매핑
 * PayApiController 에서 사용됩니다.
 */
@Data
public class ChargeRequest {
	private Long userId;      // 사용자 ID
    private int amount;       // 충전 금액
    private String method;    // 결제 수단
}
