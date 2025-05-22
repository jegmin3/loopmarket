package com.loopmarket.domain.pay.controller;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.pay.service.PayService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 결제 관련 화면 요청을 처리하는 컨트롤러입니다.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {

	private final PayService payService;

	// 페이 충전 페이지
	@GetMapping("/charge")
	public String showChargePage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/charge", model);
	}

	// 페이 환불 페이지
	@GetMapping("/refund")
	public String showRefundPage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/refund", model);
	}
	
	// 결제(안전결제) 화면 진입
	@GetMapping("/checkout")
	public String showCheckoutPage(@RequestParam("productId") Long productId, Model model) {
	    MemberEntity loginUser = getLoginUser();
	    if (loginUser == null) return "redirect:/member/login";

	    // 하드코딩된 상품 정보 (추후 productService.findById(...)로 대체 예정)
	    //Product product = productService.findById(productId);
	    Map<String, Object> product = Map.of(
	        "id", 100,
	        "title", "임시 상품 제목",
	        "price", 15000,
	        "imageUrl", "/img/pay/sample.png",
	        "tradeType", "택배거래",
	        "sellerId", 1L
	    );
	    
	    model.addAttribute("loginUser", loginUser);
	    model.addAttribute("product", product);

	    return render("pay/checkout", model);
	}
}