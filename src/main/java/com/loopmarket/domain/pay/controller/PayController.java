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
}