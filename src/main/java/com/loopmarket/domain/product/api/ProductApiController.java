package com.loopmarket.domain.product.api;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductRepository productRepository;

    /**
     * 상품 상태를 변경합니다 (판매중 / 예약중 / 판매완료)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateProductStatus(@PathVariable Long id,
                                                      @RequestBody Map<String, String> body,
                                                      HttpSession session) {
        String newStatus = body.get("status");

        // 1. 상태 유효성 검사
        List<String> validStatuses = List.of("ONSALE", "RESERVED");
        if (!validStatuses.contains(newStatus)) {
            return ResponseEntity.badRequest().body("유효하지 않은 상태입니다.");
        }

        // 2. 로그인 사용자 확인
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // 3. 상품 조회
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 4. 권한 확인 (본인만 수정 가능)
        // => loginUser.getUserId()가 Integer 타입인 경우 Long으로 변환해서 비교
        Long loginUserId = Long.valueOf(loginUser.getUserId().toString());
        if (!product.getUserId().equals(loginUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인 상품만 수정할 수 있습니다.");
        }

        // 5. 상태 변경
        product.setStatus(newStatus);
        product.setUpdateAt(LocalDateTime.now());
        productRepository.save(product);

        return ResponseEntity.ok("상태 변경 완료");
    }
    
    /**
     * 상품 숨김 처리 (true: 숨김, false: 표시)
     */
    @PatchMapping("/{id}/hide")
    public ResponseEntity<String> toggleProductHidden(@PathVariable Long id,
                                                      HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Long loginUserId = Long.valueOf(loginUser.getUserId().toString());
        if (!product.getUserId().equals(loginUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인 상품만 수정할 수 있습니다.");
        }

        // 현재 숨김 상태 반전
        product.setIsHidden(!product.getIsHidden());
        product.setUpdateAt(LocalDateTime.now());
        productRepository.save(product);

        return ResponseEntity.ok(product.getIsHidden() ? "숨김 처리됨" : "숨김 해제됨");
    }
}