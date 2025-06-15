package com.loopmarket.domain.member.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.common.service.EmailService;
import com.loopmarket.domain.member.MemberService;

@RestController
@RequestMapping("/api/account")
public class AccountRecoveryController {

    @Autowired
    private MemberService memberService;
    @Autowired
    private EmailService emailService;
    
    // 메일보내기
    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String email) {
        if (!memberService.existByEmail(email)) {
            return ResponseEntity.badRequest().body("가입된 이메일이 아닙니다.");
        }
        emailService.sendCode(email);
        return ResponseEntity.ok("인증번호가 전송되었습니다.");
    }
    // 인증번호 확인
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean verified = emailService.verify(email, code);
        return ResponseEntity.ok(Map.of("verified", verified));
    }
    // 비번초기화
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String newPassword) {
        memberService.resetPasswordByEmail(email, newPassword);
        return ResponseEntity.ok("비밀번호가 재설정되었습니다.");
    }
}

