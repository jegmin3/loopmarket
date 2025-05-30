package com.loopmarket.domain.wishlist.api;

import com.loopmarket.domain.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * 관심목록(Wishlist) API 컨트롤러
 *
 * 사용자가 상품에 대해 찜을 토글하는 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final WishlistService wishlistService;

    /**
     * 찜하기 토글 API
     *
     * @param productId 찜할 상품 ID
     * @param principal 로그인 사용자 정보
     * @return true = 찜됨 / false = 찜 해제됨
     */
    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleWishlist(@RequestParam Long productId, Principal principal) {
        // 세션 로그인 사용자 ID 가져오기
        Long userId = Long.valueOf(principal.getName());

        // 서비스 호출 (찜 추가 or 해제)
        boolean isWished = wishlistService.toggleWishlist(userId, productId);

        // true = 찜됨 / false = 해제됨
        return ResponseEntity.ok(isWished);
    }
}