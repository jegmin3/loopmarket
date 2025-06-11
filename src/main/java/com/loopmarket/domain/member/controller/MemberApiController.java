package com.loopmarket.domain.member.controller;

import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

/** 회원 관련 RESTful API를 제공하는 컨트롤러 */
@RestController // RESTful API 컨트롤러
@RequestMapping("/api/member") // 기본 URL 경로
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberRepository memberRepository;
    private final ImageService imageService;
    
    /**
     * 서버에 토큰 저장 
     */
    @PostMapping("/fcm-token")
    public void saveFcmToken(@RequestBody Map<String, String> request, HttpSession session) {
        String token = request.get("token");
        
        Object loginUser = session.getAttribute("loginUser"); // 정확한 세션 키
        if (loginUser != null && loginUser instanceof MemberEntity) {
            MemberEntity member = (MemberEntity) loginUser;

            // 동일한 토큰이면 저장 로직 생략
            if (token != null && !token.equals(member.getFcmToken())) {
                member.setFcmToken(token);
                memberRepository.save(member);
                System.out.println("로그인 사용자 토큰 저장됨: " + member.getUserId());
            } else {
                //System.out.println("동일한 토큰, 저장 생략: " + token);
            }

        } else {
            //System.out.println("로그인되지 않은 사용자의 FCM 토큰 수신: " + token);
        }
    } //saveFcmToekn끝
    
    /** 공통헤더에 사용자 드롭다운용 */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        MemberEntity user = (MemberEntity) session.getAttribute("loginUser");
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("nickname", user.getNickname());
            result.put("profile", imageService.getProfilePath(user.getUserId()));
            result.put("role", user.getRole());
        } else {
            result.put("error", "unauthorized");
        }
        return result;
    }

    
}
