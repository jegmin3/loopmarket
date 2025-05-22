package com.loopmarket.domain.mypage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import javax.servlet.http.HttpSession;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ProfileController extends BaseController {

    @Value("${upload.images.path}")
    private String uploadPath;

    private final MemberRepository memberRepository;

    public ProfileController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    // 이 부분이 핵심: String → LocalDate 변환기 등록
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    setValue(LocalDate.parse(text, dateFormatter));
                }
            }

            @Override
            public String getAsText() {
                LocalDate value = (LocalDate) getValue();
                return (value != null ? value.format(dateFormatter) : "");
            }
        });
    }
    @GetMapping("/mypage/edit")
    public String editProfileForm(Model model, HttpSession session) {
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return "redirect:/member/login";
        }
        model.addAttribute("users", member);
        return render("mypage/edit_profile", model);
    }
    

    @PostMapping("/mypage/edit")
    public String updateProfile(
            @ModelAttribute MemberEntity formMember,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpSession session,
            Model model) throws IOException {

        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        if (member == null) {
            return "redirect:/member/login";
        }

        // DB에서 최신 멤버 엔티티 조회
        member = memberRepository.findById(member.getUserId()).orElseThrow();

        // 기본 프로필 정보 업데이트
        member.setNickname(formMember.getNickname());
        member.setPhoneNumber(formMember.getPhoneNumber());
        member.setBirthdate(formMember.getBirthdate());

        // 프로필 이미지가 업로드되었으면 저장 처리
        if (profileImage != null && !profileImage.isEmpty()) {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String filename = member.getUserId() + ".png";  // 파일명은 userId.png로 고정
            Path targetLocation = uploadDir.resolve(filename);

            profileImage.transferTo(targetLocation.toFile());

            // 저장한 이미지 파일명 혹은 id를 엔티티에 반영
            member.setProfileImgId(filename);
        }

        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        // 세션도 갱신
        session.setAttribute("loginUser", member);

        
        model.addAttribute("users", member);
        return "redirect:/mypage";
    }
}