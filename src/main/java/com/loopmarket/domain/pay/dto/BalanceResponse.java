package com.loopmarket.domain.pay.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 잔액 조회 API 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private boolean success;
    private String message;
    private int balance;
}