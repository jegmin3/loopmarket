package com.loopmarket.domain.member.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.member.dto.MemberDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController extends BaseController {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@GetMapping("/login")
	public String loginGET(Model model) {
		return render("member/login", model);
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute MemberDTO dto, HttpSession session, RedirectAttributes redirectAttributes) {
	    Optional<MemberEntity> optionalMember = memberRepository.findByEmail(dto.getEmail());

	    if (optionalMember.isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 이메일입니다.");
	        //return "redirect:/member/login";
	    }

	    MemberEntity member = optionalMember.get();

//	    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
//	        redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
//	        return "redirect:/member/login";
//	    }

	    // Entity → DTO 변환
	    //MemberDTO loginUser = MemberDTO.fromEntity(member);

	    session.setAttribute("loginUser", member);

	    redirectAttributes.addFlashAttribute("successMessage", "로그인 성공!");
	    return "redirect:/";
	}
	
	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		session.invalidate(); // 세션 전체 제거 (로그아웃)
		
		redirectAttributes.addFlashAttribute("successMessage", "로그아웃 되었습니다.");
		return "redirect:/member/login"; // 로그인 페이지로 이동
	}
	
	// 회원가입
	@GetMapping("/signup")
	public String signupGET(Model model) {
		return render("member/signup", model);
	}
    
    @PostMapping("/signup")
    public String signupPOST(@ModelAttribute MemberDTO dto) {
        // MemberEntity엔티티 객체를 Builder 패턴을 이용해 생성
        MemberEntity newMember = MemberEntity.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // passwordEncoder.encode()로 암호화
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                // 지금까지 설정한 값들을 바탕으로 MemberEntity 객체를 최종 생성
                .build();

        memberRepository.save(newMember);
        
        return "redirect:/member/login";
    }

	
	
}
