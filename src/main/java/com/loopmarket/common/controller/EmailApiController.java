package com.loopmarket.common.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.common.service.EmailService;

import lombok.RequiredArgsConstructor;

/** 이메일 인증 API */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailApiController {

    private final EmailService emailService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String email) {
        emailService.sendCode(email);
        return ResponseEntity.ok("인증코드 전송 완료");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean result = emailService.verify(email, code);
        return ResponseEntity.ok(Map.of("verified", result));
    }
}
