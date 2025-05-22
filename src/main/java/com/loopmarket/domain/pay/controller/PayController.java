package com.loopmarket.domain.pay.controller;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.pay.service.PayService;

import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 결제 관련 화면 요청을 처리하는 컨트롤러입니다.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {
	
	// 페이 충전 페이지
	@GetMapping("/charge")
	public String showChargePage(HttpSession session, Model model) {
	    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

	    // 로그인 안 한 경우 로그인 페이지로 리다이렉트
	    if (loginUser == null) {
	        return "redirect:/member/login";
	    }
	    
	    model.addAttribute("userId", loginUser.getUserId());
	    model.addAttribute("userName", loginUser.getNickname());
	    model.addAttribute("userEmail", loginUser.getEmail());

	    return render("pay/charge", model);
	}
	
	// 페이 환불 페이지
	@GetMapping("/refund")
    public String showRefundPage(HttpSession session, Model model) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/member/login";
        }

        model.addAttribute("userId", loginUser.getUserId());
        model.addAttribute("userName", loginUser.getNickname());
        model.addAttribute("userEmail", loginUser.getEmail());

        return render("pay/refund", model);
    }
}