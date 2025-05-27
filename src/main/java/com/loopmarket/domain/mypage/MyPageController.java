package com.loopmarket.domain.mypage;

import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.member.dto.MemberDTO;
import com.loopmarket.domain.pay.dto.ConfirmableItem;
import com.loopmarket.domain.pay.service.PayService;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.product.service.ProductService;
import com.loopmarket.domain.purchase.repository.PurchaseRepository;
import com.loopmarket.common.controller.BaseController;

import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mypage")
public class MyPageController extends BaseController {

	private final ProductRepository productRepository;
	private final PurchaseRepository purchaseRepository;
	private final List<String> ongoingStatuses = List.of("ONSALE", "RESERVED");
	private final ProductService productService;
	private final ImageService imageService;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;
	private final PayService payService;

	@Autowired
	public MyPageController(ProductRepository productRepository, PurchaseRepository purchaseRepository,
			ProductService productService, ImageService imageService, PasswordEncoder passwordEncoder,
			MemberRepository memberRepository, PayService payService) {
		this.productRepository = productRepository;
		this.purchaseRepository = purchaseRepository;
		this.productService = productService;
		this.imageService = imageService;
		this.passwordEncoder = passwordEncoder;
		this.memberRepository = memberRepository;
		this.payService = payService;
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
		// 추가 - jw
		model.addAttribute("loginUser", member); 

		long totalSalesCount = productRepository.countByUserId(member.getUserId().longValue());
		long totalPurchaseCount = purchaseRepository.countByBuyerId(member.getUserId().longValue());

		model.addAttribute("totalSalesCount", totalSalesCount);
		model.addAttribute("totalPurchaseCount", totalPurchaseCount);

		// 프로필 이미지 경로 생성 후 모델에 추가
		String profileImagePath = productService.getProfileImagePath(member.getProfileImgId());
		model.addAttribute("profileImagePath", profileImagePath);
		
		// 구매 확정 가능한 상품 목록 confirmables 추가 - jw
		List<ConfirmableItem> confirmables = payService.getConfirmablePayments(member.getUserId().longValue());
		model.addAttribute("confirmables", confirmables);

		// 판매 중인 상품 목록 (QR 생성용) myProducts 추가 - jw
		List<ProductEntity> myProducts = productService.getMyProducts(member.getUserId().longValue());
		for (ProductEntity product : myProducts) {
			String thumbnailPath = productService.getThumbnailPath(product.getProductId());
			product.setThumbnailPath(thumbnailPath);
		}
		model.addAttribute("myProducts", myProducts);

		return render("mypage/mypage", model);
	}

	// 현재 판매중인 상품
	@GetMapping("/products")
	public String mySellingItems(HttpServletRequest request, HttpSession session, Model model) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null) {
			return "redirect:/member/login";
		}

		List<ProductEntity> myProducts = productRepository.findByUserIdAndStatusIn(member.getUserId().longValue(),
				ongoingStatuses);

		for (ProductEntity product : myProducts) {
			String thumbnailPath = imageService.getThumbnailPath(product.getProductId());
			product.setThumbnailPath(thumbnailPath);

			List<String> imagePaths = imageService.getAllImagePaths(product.getProductId());
			product.setImagePaths(imagePaths);
		}

		model.addAttribute("products", myProducts);

		return renderMypage(request, model, "mypage/my_selling_items");
	}

	// 판매완료상품 내역
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

	// 비밀번호 변경
	@GetMapping("/change_password")
	public String changePasswordForm(Model model, HttpSession session) {
		MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
		if (member == null) {
			return "redirect:/member/login";
		}
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

		if (dbMember == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "사용자 정보를 찾을 수 없습니다.");
			return "redirect:/member/login";
		}

		if (!passwordEncoder.matches(currentPassword, dbMember.getPassword())) {
			redirectAttributes.addFlashAttribute("errorMessage", "현재 비밀번호가 일치하지 않습니다.");
			return "redirect:/mypage/change_password";
		}

		dbMember.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(dbMember);

		// 세션에 최신 정보로 다시 세팅
		session.setAttribute("loginUser", dbMember);

		redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
		return "redirect:/mypage";
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