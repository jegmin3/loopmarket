package com.loopmarket.domain.member;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.dto.MemberDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController extends BaseController {
	
	private final MemberRepository memberRepository;
	
	@GetMapping("/login")
	public String loginGET(Model model) {
		return render("member/login", model);
	}
	
	@GetMapping("/signup")
	public String signupGET(Model model) {
		return render("member/signup", model);
	}
	
    @PostMapping("/signup")
    public String signupPOST(@ModelAttribute MemberDTO dto, Model model) {

        if (memberRepository.existsByEmail(dto.getEmail())) {
            model.addAttribute("errorMessage", "이미 사용 중인 이메일입니다.");
            return "member/signup"; // 다시 회원가입 페이지로
        }

        if (memberRepository.existsByNickname(dto.getNickname())) {
            model.addAttribute("errorMessage", "이미 사용 중인 닉네임입니다.");
            return "member/signup";
        }
        
        // 비밀번호는 추후 암호화 예정
        MemberEntity newMember = MemberEntity.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())  // 비밀번호 암호화 필요
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                .build();
        
        memberRepository.save(newMember);

        return "redirect:/member/login";
    }
	
	
}
