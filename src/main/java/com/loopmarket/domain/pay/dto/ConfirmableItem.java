package com.loopmarket.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmableItem {
    private Long productId;
    private Long paymentId;
    private String title;
    private Integer price;
    private String thumbnailPath;
}