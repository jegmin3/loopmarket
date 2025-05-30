package com.loopmarket.domain.wishlist.service;

import com.loopmarket.domain.wishlist.entity.Wishlist;
import com.loopmarket.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 관심목록(Wishlist) 서비스
 *
 * 사용자 찜 목록 추가/제거 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    /**
     * 찜 토글 처리
     * 이미 찜한 상태면 해제, 아니면 새로 추가
     *
     * @param userId 사용자 ID
     * @param prodId 상품 ID
     * @return true = 찜됨 / false = 찜 해제됨
     */
    public boolean toggleWishlist(Long userId, Long prodId) {
        // 이미 찜한 내역이 있는지 확인
        Optional<Wishlist> existing = wishlistRepository.findByUserIdAndProdId(userId, prodId);

        if (existing.isPresent()) {
            // 있으면 삭제 (찜 해제)
            wishlistRepository.delete(existing.get());
            return false;
        } else {
            // 없으면 새로 저장 (찜 추가)
            Wishlist wishlist = Wishlist.builder()
                    .userId(userId)
                    .prodId(prodId)
                    .build();
            wishlistRepository.save(wishlist);
            return true;
        }
    }
}