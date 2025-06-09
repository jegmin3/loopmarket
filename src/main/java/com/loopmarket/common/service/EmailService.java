package com.loopmarket.common.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** 이메일 인증용 서비스 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final Map<String, String> emailAuthMap = new ConcurrentHashMap<>();

    public void sendCode(String email) {
        String code = UUID.randomUUID().toString().substring(0, 8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[LoopMarket] 이메일 인증 코드");
        message.setText("인증 코드는 다음과 같습니다: " + code);
        mailSender.send(message);

        emailAuthMap.put(email, code); // 코드 저장
    }

    public boolean verify(String email, String inputCode) {
        return inputCode.equals(emailAuthMap.get(email));
    }
}

