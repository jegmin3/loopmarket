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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;

import com.loopmarket.domain.admin.dashboard.Entity.LoginHistory;
import com.loopmarket.domain.admin.dashboard.repository.LoginHistoryRepository;
import com.loopmarket.domain.admin.user.UserStatusEntity;
import com.loopmarket.domain.admin.user.UserStatusRepository;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.member.MemberService;
import com.loopmarket.domain.member.dto.MemberDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController extends BaseController {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final MemberService memberService;
	private final UserStatusRepository userStatusRepository;
	
	// 로그인 시간기록용
	private final LoginHistoryRepository loginHistoryRepository;


	@GetMapping("/login")
	public String loginGET(Model model) {
		return render("member/login", model);
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute MemberDTO dto, HttpSession session, RedirectAttributes redirectAttributes) {
	    Optional<MemberEntity> optionalMember = memberRepository.findByEmail(dto.getEmail());

	    // 기본값
	    Integer userId = null;
	    String result = "FAIL";
	    
	    if (optionalMember.isEmpty()) {
	        redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 이메일입니다.");
	        return "redirect:/member/login";
	    }

	    MemberEntity member = optionalMember.get();
	    
	    // 로그인 기록용
	    userId = member.getUserId();
	    
	    // 계정 상태 확인
	    UserStatusEntity status = userStatusRepository.findById(userId).orElse(null);
	    if (status != null && status.getAccountStatus() == UserStatusEntity.AccountStatus.SUSPENDED) {
	        redirectAttributes.addFlashAttribute("errorMessage", "정지된 계정입니다. 관리자에게 문의하세요.");
	        
	        return "redirect:/member/login";
	    }

	    // 비밀번호 체크
	    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
	        redirectAttributes.addFlashAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
	        return "redirect:/member/login";
	    }

	    // Entity → DTO 변환
	    //MemberDTO loginUser = MemberDTO.fromEntity(member);

	    session.setAttribute("loginUser", member);
	    session.setAttribute("userRole", member.getRole().name());

	    redirectAttributes.addFlashAttribute("successMessage", "로그인 성공!");

	    result = "SUCCESS";
	    
	    // 로그인 이력 저장
	    if (userId != null) {
	        LoginHistory history = new LoginHistory();
	        history.setUserId(userId);
	        history.setLoginResult(result);
	        loginHistoryRepository.save(history);
	    }
	    
	    return "redirect:/";
	}

	// 로그아웃
	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
		session.invalidate(); // 세션 전체 제거 (로그아웃)

		redirectAttributes.addFlashAttribute("successMessage", "로그아웃 되었습니다.");
		return "redirect:/member/login"; // 로그인 페이지로 이동
	}

	// 회원가입으로 이동
	@GetMapping("/signup")
	public String signupGET(Model model) {
		return render("member/signup", model);
	}
	// 회원가입 폼 제출 시
    @PostMapping("/signup")
    public String signupPOST(@ModelAttribute MemberDTO dto, RedirectAttributes redirectAttributes) {
        // MemberEntity엔티티 객체를 Builder 패턴을 이용해 생성
        MemberEntity newMember = MemberEntity.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // passwordEncoder.encode()로 암호화
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                // 지금까지 설정한 값들을 바탕으로 MemberEntity 객체를 최종 생성
                .build();
        
        memberRepository.save(newMember);
        
        redirectAttributes.addFlashAttribute("successMessage", "회원가입 되었습니다! 로그인 해주세요.");
        return "redirect:/member/login";
    }
    // 비번 변경 페이지로
    @GetMapping("/reset-password")
    public String goPasswordReset(Model model) 
    { return render("member/pwReset", model); }
    
    // 강제 비밀번호 변경
    @GetMapping("force_change_password")
    public String showForcePasswordChangeForm() {
        return "/member/force_change_password"; // 템플릿 파일 위치
    }
    
    @PostMapping("/force_change_password")
    public String forceChangePassword(
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword,
            Model model) {

        boolean success = memberService.changePasswordByEmail(email, newPassword);
        if(success) {
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/member/login";  // 로그인 페이지로 이동
        } else {
            model.addAttribute("error", "비밀번호 변경에 실패했습니다.");
            return "member/force_change_password"; // 다시 변경 폼으로
        }
    }



}