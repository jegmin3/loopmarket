package com.loopmarket.domain.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 찜한 상품 간단 조회용 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDto {
    private Long productId;
    private String title;
    private String thumbnailPath;
    private int price;
    private String status;
}