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
import com.loopmarket.domain.product.service.ProductService;

import javax.servlet.http.HttpSession;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ProfileController extends BaseController {

	 // 프로필 이미지를 저장하는 실제 서버 폴더 경로 (로컬 저장 경로)
    private final String uploadPath = System.getProperty("user.dir") + "/upload/images/profiles/";

    private final MemberRepository memberRepository;

    private final ProductService productService;

    public ProfileController(MemberRepository memberRepository, ProductService productService) {
        this.memberRepository = memberRepository;
        this.productService = productService;
    }
    
    // 이 부분이 핵심: String → LocalDate 변환기 등록
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
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

        // productService.getProfileImagePath는 이미 /images/profiles/ 경로 붙여서 반환하도록 되어있다면, 그대로 쓰면 됩니다.
        String profileImagePath = productService.getProfileImagePath(member.getProfileImgId());

        model.addAttribute("users", member);
        model.addAttribute("profileImagePath", profileImagePath);

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

        member = memberRepository.findById(member.getUserId()).orElseThrow();

        member.setNickname(formMember.getNickname());
        member.setPhoneNumber(formMember.getPhoneNumber());
        member.setBirthdate(formMember.getBirthdate());

        if (profileImage != null && !profileImage.isEmpty()) {
            String filename = member.getUserId() + "_profile.png";

            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();

            if (!uploadDir.toFile().exists()) {
                uploadDir.toFile().mkdirs();
            }

            Path targetLocation = uploadDir.resolve(filename);
            profileImage.transferTo(targetLocation.toFile());

            member.setProfileImgId(filename);
        }

        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        session.setAttribute("loginUser", member);

        model.addAttribute("users", member);

        return "redirect:/mypage";
    }
}