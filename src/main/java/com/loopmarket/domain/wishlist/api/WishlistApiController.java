package com.loopmarket.domain.wishlist.api;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관심목록(Wishlist) API 컨트롤러
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController extends BaseController {

    private final WishlistService wishlistService;

    // 찜 토글 API
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleWishlist(@RequestParam Long productId) {
        MemberEntity loginUser = getLoginUser();
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = loginUser.getUserId().longValue();
        boolean isWished = wishlistService.toggleWishlist(userId, productId);
        return ResponseEntity.ok(isWished);
    }

    // 찜 여부 확인 API
    @GetMapping("/is-wished")
    public ResponseEntity<?> isWished(@RequestParam Long productId) {
        MemberEntity loginUser = getLoginUser();
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = loginUser.getUserId().longValue();
        boolean isWished = wishlistService.isWished(userId, productId);
        return ResponseEntity.ok(isWished);
    }

    // 사용자의 찜한 상품 목록 조회 API
    @GetMapping("/list")
    public ResponseEntity<?> getWishlistProductIds() {
        MemberEntity loginUser = getLoginUser();
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = loginUser.getUserId().longValue();
        List<Long> productIds = wishlistService.getWishlistProductIds(userId);
        return ResponseEntity.ok(productIds);
    }

    // 찜 해제 전용 API
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeWishlist(@PathVariable Long productId) {
        MemberEntity loginUser = getLoginUser();
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long userId = loginUser.getUserId().longValue();
        wishlistService.removeWishlist(userId, productId);
        return ResponseEntity.ok("찜 해제가 완료되었습니다.");
    }
}