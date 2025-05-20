package com.loopmarket.domain.pay.controller;

import com.loopmarket.common.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {

    @GetMapping("/charge")
    public String showChargePage(Model model) {
        // 실 로그인 연동 전까지는 하드코딩된 테스트 사용자
        model.addAttribute("userId", 1L);
        model.addAttribute("userName", "테스트");
        model.addAttribute("userEmail", "test@itwill.com");

        // 레이아웃을 포함한 전체 페이지 반환
        return render("pay/charge", model);
    }
}