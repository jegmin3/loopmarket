package com.loopmarket.domain.member;

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
import com.loopmarket.domain.member.dto.LoginDTO;
import com.loopmarket.domain.member.dto.MemberDTO;
import com.loopmarket.domain.member.dto.SignupDTO;

@Controller
@RequestMapping("/member")
public class MemberController extends BaseController {
	
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	
	// BaseController에 기본생성자가 없는데 super()를 호출하려다 실패해서 오류남으로 인해
	// requiredArgsConstructor 안쓰고 직접 생성자 했음
    public MemberController(HttpSession session, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        super(session); // 명시적으로 부모 생성자 호출
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
	@GetMapping("/login")
	public String loginGET(Model model) {
		return render("member/login", model);
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute LoginDTO dto, HttpSession session, RedirectAttributes redirectAttributes) {
	    Optional<MemberEntity> optionalMember = memberRepository.findByEmail(dto.getEmail());

	    if (optionalMember.isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 이메일입니다.");
	        return "redirect:/member/login";
	    }

	    MemberEntity member = optionalMember.get();

	    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
	        redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
	        return "redirect:/member/login";
	    }

	    // Entity → DTO 변환
	    MemberDTO loginUser = MemberDTO.fromEntity(member);

	    // 세션에 DTO 저장
	    session.setAttribute("loginUser", loginUser);

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
    public String signupPOST(@ModelAttribute SignupDTO dto) {
        // 비밀번호는 추후 암호화 필요
        MemberEntity newMember = MemberEntity.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // passwordEncoder.encode()로 암호화
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        memberRepository.save(newMember);
        return "redirect:/member/login";
    }

	
	
}
