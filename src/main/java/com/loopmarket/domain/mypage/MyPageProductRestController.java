package com.loopmarket.domain.mypage;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/mypage/api/products")
@RequiredArgsConstructor
public class MyPageProductRestController {

    private final ProductService productService;
    private final MemberRepository memberRepository;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMyProduct(@PathVariable Long id, HttpSession session) {
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        ProductEntity product = productService.findById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        // 본인 상품인지 체크
        if (!product.getUserId().equals(member.getUserId().longValue())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인 상품만 삭제 가능합니다.");
        }

        productService.deleteProductById(id);
        return ResponseEntity.ok("상품이 삭제되었습니다.");
    }
}