package com.loopmarket.domain.mypage;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.pay.dto.ConfirmableItem;
import com.loopmarket.domain.pay.service.PayService;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.product.service.ProductService;
import com.loopmarket.domain.purchase.repository.PurchaseRepository;
import com.loopmarket.domain.wishlist.dto.WishlistDto;
import com.loopmarket.domain.wishlist.service.WishlistService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController extends BaseController {

	private final ProductRepository productRepository;
	private final PurchaseRepository purchaseRepository;
	private final ProductService productService;
	private final ImageService imageService;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final PayService payService;
	private final WishlistService wishlistService;

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
		// 추가 - jw
		model.addAttribute("loginUser", member);

		model.addAttribute("totalSalesCount", productRepository.countByUserId(member.getUserId().longValue()));
		model.addAttribute("totalPurchaseCount", purchaseRepository.countByBuyerId(member.getUserId().longValue()));

		// 프로필 이미지 경로 생성 후 모델에 추가
		String profileImagePath = productService.getProfileImagePath(member.getProfileImgId());
		model.addAttribute("profileImagePath", profileImagePath);

		// 구매 확정 가능한 상품 목록 confirmables 추가 - jw
		List<ConfirmableItem> confirmables = payService.getConfirmablePayments(member.getUserId().longValue());
		model.addAttribute("confirmables", confirmables);

		// 판매중 상태만 필터링 (ONSALE, RESERVED)
		List<ProductEntity> myProducts = productService.getOngoingProducts(member.getUserId().longValue());
		model.addAttribute("myProducts", myProducts);

		return render("mypage/mypage", model);
	}

	// 현재 판매중인 상품
	@GetMapping("/products")
	public String mySellingItems(HttpServletRequest request, HttpSession session, Model model) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null)
			return "redirect:/member/login";

		List<ProductEntity> myProducts = productService.getOngoingProducts(member.getUserId().longValue());
		for (ProductEntity product : myProducts) {
			product.setThumbnailPath(imageService.getThumbnailPath(product.getProductId()));
			product.setImagePaths(imageService.getAllImagePaths(product.getProductId()));
		}
		model.addAttribute("products", myProducts);
		return renderMypage(request, model, "mypage/my_selling_items");
	}

	// 판매완료상품 내역
	@GetMapping("/sales")
	public String mySalesHistory(HttpServletRequest request, HttpSession session, Model model) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null)
			return "redirect:/member/login";

		model.addAttribute("soldProducts", productService.getSoldProductsWithThumbnail(member.getUserId().longValue()));
		return renderMypage(request, model, "mypage/my_sales_history");
	}

	// 구매 내역
	@GetMapping("/purchase")
	public String myPurchaseHistory(HttpServletRequest request, HttpSession session, Model model) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null)
			return "redirect:/member/login";

		model.addAttribute("purchasedProducts", payService.getMyPurchaseHistory(member.getUserId().longValue()));
		return renderMypage(request, model, "mypage/mypage-purchase");
	}

	// 비밀번호 변경
	@GetMapping("/change_password")
	public String changePasswordForm(Model model, HttpSession session) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null)
			return "redirect:/member/login";
		return render("mypage/change_password", model);
	}

	@PostMapping("/change_password")
	public String changePassword(@RequestParam("currentPassword") String currentPassword,
			@RequestParam("newPassword") String newPassword, HttpSession session,
			RedirectAttributes redirectAttributes) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}

		MemberEntity dbMember = memberRepository.findById(member.getUserId()).orElse(null);
		if (dbMember == null || !passwordEncoder.matches(currentPassword, dbMember.getPassword())) {
			redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
			return "redirect:/mypage/change_password";
		}

		dbMember.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(dbMember);
		session.setAttribute("loginUser", dbMember);
		redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
		return "redirect:/mypage";
	}
	
	// 찜한 상품 목록 보기
	@GetMapping("/wishlist")
	public String myWishlist(HttpServletRequest request, HttpSession session, Model model) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null) {
			return "redirect:/member/login";
		}

		List<WishlistDto> wishlistItems = wishlistService.getWishlistDtos(member.getUserId().longValue());
		model.addAttribute("wishlistItems", wishlistItems);

		return renderMypage(request, model, "mypage/my_wishlist");
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