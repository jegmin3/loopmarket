package com.loopmarket.domain.wishlist.service;

import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.wishlist.dto.WishlistDto;
import com.loopmarket.domain.wishlist.entity.Wishlist;
import com.loopmarket.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 관심목록(Wishlist) 서비스
 *
 * 사용자 찜 목록 추가/제거 및 조회 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ImageService imageService;

    // 찜 토글 처리: 이미 있으면 제거, 없으면 추가
    public boolean toggleWishlist(Long userId, Long prodId) {
        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndProdId(userId, prodId);

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return false;
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .userId(userId)
                    .prodId(prodId)
                    .build();
            wishlistRepository.save(wishlist);
            return true;
        }
    }

    // 사용자가 특정 상품을 찜했는지 여부 확인
    public boolean isWished(Long userId, Long prodId) {
        return wishlistRepository.findByUserIdAndProdId(userId, prodId).isPresent();
    }

    // 사용자의 찜한 상품 ID 목록 조회
    public List<Long> getWishlistProductIds(Long userId) {
        return wishlistRepository.findAllByUserId(userId)
                .stream()
                .map(Wishlist::getProdId)
                .collect(Collectors.toList()); // Java 11 호환
    }
    
    @Transactional
    // 찜 해제 전용 API 처리용 메서드
    public void removeWishlist(Long userId, Long prodId) {
        wishlistRepository.deleteByUserIdAndProdId(userId, prodId);
    }

    // 찜 목록을 WishlistDto로 변환해서 반환
    // 찜 목록을 WishlistDto로 변환해서 반환 (숨김 상품 제외)
    public List<WishlistDto> getWishlistDtos(Long userId) {
        List<Long> prodIds = getWishlistProductIds(userId);

        return productRepository.findAllById(prodIds).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsHidden())) // 숨김 상품 제외
                .map(p -> WishlistDto.builder()
                        .productId(p.getProductId())
                        .title(p.getTitle())
                        .thumbnailPath(getThumbnail(p.getProductId()))
                        .price(p.getPrice())
                        .status(p.getStatus()) // 상태는 그대로 DTO에 담음
                        .build())
                .collect(Collectors.toList());
    }

    private String getThumbnail(Long productId) {
        String path = imageService.getThumbnailPath(productId);
        return path != null ? path : "/images/no-image.png";
    }
}