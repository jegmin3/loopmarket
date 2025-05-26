package com.loopmarket.domain.pay.enums;

/**
 * 결제 수단
 */
public enum PaymentMethod {
	PAY,        // 자체페이
	KAKAOPAY,   // 카카오페이
	TOSSPAY,    // 토스페이
	KG_INICIS,  // KG이니시스
	SAFE_PAY    // 안전결제 처리 전용
}