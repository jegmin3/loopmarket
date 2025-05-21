package com.loopmarket.domain.member;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	
	@PostMapping("/login")
	public String login(@ModelAttribute MemberDTO dto, HttpSession session, RedirectAttributes redirectAttributes) {
		Optional<MemberEntity> optionalMember = memberRepository.findByEmail(dto.getEmail());
		
		MemberEntity member = optionalMember.get();
		
		// 로그인 성공시 세션에 사용자 정보 저장
		session.setAttribute("loginUser", member);
		
		redirectAttributes.addFlashAttribute("successMessage", "로그인 되었습니다.");
		return "redirect:/"; // 로그인 성공 시 메인 페이지로 이동
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		session.invalidate(); // 세션 전체 제거 (로그아웃)
		
		redirectAttributes.addFlashAttribute("successMessage", "로그아웃 되었습니다.");
		return "redirect:/member/login"; // 로그인 페이지로 이동
	}
	
	@GetMapping("/signup")
	public String signupGET(Model model) {
		return render("member/signup", model);
	}
	
    @PostMapping("/signup")
    public String signupPOST(@ModelAttribute MemberDTO dto) {
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
