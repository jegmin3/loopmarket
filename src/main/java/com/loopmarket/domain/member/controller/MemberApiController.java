package com.loopmarket.domain.member.controller;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/** 회원 관련 RESTful API를 제공하는 컨트롤러 */
@RestController // RESTful API 컨트롤러
@RequestMapping("/api/member") // 기본 URL 경로
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberRepository memberRepository;
    
    /**
     * 서버에 토큰 저장 
     */
    @PostMapping("/fcm-token")
    public void saveFcmToken(@RequestBody Map<String, String> request, HttpSession session) {
        String token = request.get("token");
        MemberEntity member = (MemberEntity) session.getAttribute("loginUser");
        member.setFcmToken(token);
        memberRepository.save(member);
    }

}
