package com.loopmarket.domain.pay.dto;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
}