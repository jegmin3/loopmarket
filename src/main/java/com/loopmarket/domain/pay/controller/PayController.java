package com.loopmarket.domain.pay.controller;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.pay.enums.PaymentStatus;
import com.loopmarket.domain.pay.repository.PaymentRepository;
import com.loopmarket.domain.pay.service.PayService;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 결제 관련 화면 요청을 처리하는 컨트롤러입니다.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {

	private final PayService payService;
	private final ProductService productService;
    private final PaymentRepository paymentRepository;

	// 페이 충전 페이지
	@GetMapping("/charge")
	public String showChargePage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/charge", model);
	}

	// 페이 환불 페이지
	@GetMapping("/refund")
	public String showRefundPage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/refund", model);
	}

	// 결제(안전결제) 화면 진입
	@GetMapping("/checkout")
	public String showCheckoutPage(@RequestParam("productId") Long productId, Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";
		
		// 상품 정보 DB에서 조회
		ProductEntity product = productService.getProductById(productId);
		
		// 거래 중(HOLD), 정산 완료(COMPLETED)된 경우 차단
	    List<PaymentStatus> forbiddenStatuses = List.of(PaymentStatus.HOLD, PaymentStatus.COMPLETED);
	    if (paymentRepository.existsByProductIdAndStatusIn(productId, forbiddenStatuses)) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미 거래 중이거나 완료된 상품입니다.");
	    }

	    // 숨김 처리된 상품 차단
	    if (Boolean.TRUE.equals(product.getIsHidden())) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 상품은 더 이상 결제가 불가능합니다.");
	    }
		
		// 이미지 - 추후 변경 필요
		String imageUrl = "/img/pay/sample.png";
		//String imageUrl = (product.getImageUrl() != null) ? product.getImageUrl() : "/img/pay/sample.png";

		String tradeType = product.getIsDelivery() != null && product.getIsDelivery()
			    ? "택배거래"
			    : product.getIsDirect() != null && product.getIsDirect()
			      ? "직거래"
			      : "기타";

		model.addAttribute("loginUser", loginUser);
		model.addAttribute("product", product);
		model.addAttribute("imageUrl", imageUrl);
		model.addAttribute("tradeType", tradeType);

		return render("pay/checkout", model);
	}
}