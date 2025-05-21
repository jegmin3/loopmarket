package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 충전 API 응답 DTO
 * PayApiController 에서 사용됩니다.
 */
@Data
@AllArgsConstructor
public class ChargeResponse {
	private boolean success;	// 성공 여부
	private String message;		// 사용자 메시지
	private int currentBalance;		// 충전 후 잔액
}
