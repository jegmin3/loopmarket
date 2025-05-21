package com.loopmarket.domain.pay.controller;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {

	@GetMapping("/charge")
	public String showChargePage(HttpSession session, Model model) {
	    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

	    // 로그인 안 한 경우 리다이렉트
	    if (loginUser == null) {
	        return "redirect:/member/login";
	    }

	    model.addAttribute("userId", loginUser.getUserId());
	    model.addAttribute("userName", loginUser.getNickname());
	    model.addAttribute("userEmail", loginUser.getEmail());

	    return render("pay/charge", model);
	}

}