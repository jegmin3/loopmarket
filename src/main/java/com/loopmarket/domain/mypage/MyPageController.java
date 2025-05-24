package com.loopmarket.domain.mypage;

import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.member.dto.MemberDTO;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.product.service.ProductService;
import com.loopmarket.domain.purchase.repository.PurchaseRepository;
import com.loopmarket.common.controller.BaseController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Arrays;

import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/mypage")
public class MyPageController extends BaseController {
	
	private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final List<String> ongoingStatuses = List.of("ONSALE", "RESERVED");
    private final ProductService productService;
    private final ImageService imageService;
    
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    public MyPageController(ProductRepository productRepository, PurchaseRepository purchaseRepository,
                            ProductService productService, ImageService imageService) {
        this.productRepository = productRepository;
        this.purchaseRepository = purchaseRepository;
        this.productService = productService;
        this.imageService = imageService;
    }
    

    private String renderMypage(HttpServletRequest request, Model model, String viewName) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return viewName + " :: content";
        } else {
            return render(viewName, model);
        }
    }

    // 마이페이지 기본 진입
    @GetMapping
    public String myPageHome(Model model, HttpSession session) {
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return "redirect:/member/login";
        }

        member = memberRepository.findById(member.getUserId())
            .orElseThrow(() -> new IllegalStateException("사용자 정보가 없습니다."));

        session.setAttribute("loginUser", member);

        model.addAttribute("users", member);

        long totalSalesCount = productRepository.countByUserId(member.getUserId().longValue());
        long totalPurchaseCount = purchaseRepository.countByBuyerId(member.getUserId().longValue());

        model.addAttribute("totalSalesCount", totalSalesCount);
        model.addAttribute("totalPurchaseCount", totalPurchaseCount);

        // 프로필 이미지 경로 생성 후 모델에 추가
        String profileImagePath = productService.getProfileImagePath(member.getProfileImgId());
        model.addAttribute("profileImagePath", profileImagePath);

        return render("mypage/mypage", model);
    }

    
    @GetMapping("/products")
    public String mySellingItems(HttpServletRequest request, HttpSession session, Model model) {
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return "redirect:/member/login";
        }

        List<ProductEntity> myProducts = productRepository.findByUserIdAndStatusIn(member.getUserId().longValue(), ongoingStatuses);

        for (ProductEntity product : myProducts) {
            String thumbnailPath = imageService.getThumbnailPath(product.getProductId());
            product.setThumbnailPath(thumbnailPath);

            List<String> imagePaths = imageService.getAllImagePaths(product.getProductId());
            product.setImagePaths(imagePaths);
        }

        model.addAttribute("products", myProducts);

        return renderMypage(request, model, "mypage/my_selling_items");
    }
    
    @GetMapping("/sales")
    public String mySalesHistory(HttpServletRequest request, HttpSession session, Model model) {
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return "redirect:/member/login";
        }

        List<ProductEntity> soldProducts = productService.getSoldProductsWithThumbnail(member.getUserId().longValue());

        model.addAttribute("soldProducts", soldProducts);

        return renderMypage(request, model, "mypage/my_sales_history");
    }
    
    
    
//    @PostMapping("/edit") 
//    public String updateProfile(@ModelAttribute MemberEntity formMember, HttpSession session, Model model) {
//        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
//        if (member == null) {
//            return "redirect:/member/login";
//        }
//
//        // DB에서 최신 멤버 엔티티 조회
//        member = memberRepository.findById(member.getUserId()).orElseThrow();
//
//        // 변경 가능 필드만 업데이트
//        member.setNickname(formMember.getNickname());
//        member.setPhoneNumber(formMember.getPhoneNumber());
//        member.setBirthdate(formMember.getBirthdate());
//        member.setProfileImgId(formMember.getProfileImgId());
//
//        member.setUpdatedAt(java.time.LocalDateTime.now());
//
//        memberRepository.save(member);
//
//        // 세션 갱신
//        session.setAttribute("loginUser", member);
//
//        return render("mypage/mypage", model);
//    }
    
	 /* 
	 * // 상품 삭제 (AJAX)
	 * 
	 * @PostMapping("/selling/delete/{id}")
	 * 
	 * @ResponseBody public String deleteMyProduct(@PathVariable("id") Long id,
	 * Principal principal) { ProductEntity product =
	 * productRepository.findById(id).orElse(null); if (product != null &&
	 * product.getSeller().equals(principal.getName())) {
	 * productRepository.delete(product); return "success"; } return "error"; }
	 */
}