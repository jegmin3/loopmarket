package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 잔액 조회 API 응답 DTO
 */
@Data
@AllArgsConstructor
public class BalanceResponse {
	private boolean success;
	private String message;
	private int balance;
}