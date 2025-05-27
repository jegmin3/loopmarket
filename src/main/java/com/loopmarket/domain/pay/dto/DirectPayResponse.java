package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 즉시결제 처리 결과를 담는 응답 DTO
 * 결제 성공 여부, 메시지, 판매자의 최종 잔액 포함
 */
@Getter
@AllArgsConstructor
public class DirectPayResponse {

    private boolean success;
    private String message;
    private int sellerBalance;
}