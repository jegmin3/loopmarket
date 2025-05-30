package com.loopmarket.domain.pay.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 구매자가 결제한 상품 정보를 담는 DTO.
 * - 안전결제 또는 즉시결제 내역 중 구매자에게 표시할 정보만 포함합니다.
 * - 상품명, 썸네일, 결제 금액, 결제일 등 UI에서 사용할 필드를 포함합니다.
 */
@Data
@AllArgsConstructor
public class ConfirmableItem {
    private Long productId;
    private Long paymentId;
    private String title;
    private Integer price;
    private String thumbnailPath;
    private LocalDateTime createdAt;
}