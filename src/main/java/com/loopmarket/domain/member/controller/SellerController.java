package com.loopmarket.domain.member.controller;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController extends BaseController {

    private final MemberRepository memberRepository;
    private final ProductService productService;
    private final ImageService imageService;

    @GetMapping("/{id}")
    public String sellerProfile(@PathVariable("id") Integer sellerId,
                                HttpServletRequest request,
                                HttpSession session,
                                Model model) {

        // 판매자 조회
        MemberEntity seller = memberRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 로그인한 사용자 확인
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        // 로그인한 사용자와 판매자가 같으면 → 마이페이지로 이동
        if (loginUser != null && loginUser.getUserId().equals(seller.getUserId())) {
            return "redirect:/mypage";
        }

        // 판매자가 판매 중인 상품 목록 조회
        List<ProductEntity> sellingProducts = productService.getOngoingProducts(seller.getUserId().longValue());

        // 썸네일 처리
        for (ProductEntity product : sellingProducts) {
            product.setThumbnailPath(product.getImagePaths().isEmpty()
                    ? "/img/no-image.png"
                    : product.getImagePaths().get(0));
        }
        
        String profileImagePath = imageService.getProfilePath(seller.getUserId());
        
        // 모델에 데이터 추가
        model.addAttribute("seller", seller);
        model.addAttribute("sellingProducts", sellingProducts);
        model.addAttribute("profileImagePath", profileImagePath);
        model.addAttribute("loginUser", loginUser);

        // 템플릿 렌더링
        return render("mypage/seller-profile", model);
    }
}