package com.loopmarket.domain.alram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.alram.service.AlramService;
import com.loopmarket.domain.member.MemberEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alram")
public class AlramApiController extends BaseController {

    private final AlramService alramService;

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadAlrams(RedirectAttributes redirectAttributes) {
        MemberEntity loginUser = getLoginUser();

        List<AlramEntity> unreadList = alramService.getUnreadAlrams(loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("count", unreadList.size());
        result.put("notifications", unreadList);

        return ResponseEntity.ok(result);
    }
    // 알림 읽음처리
    @PatchMapping("/mark-read")
    public ResponseEntity<?> markAlramsAsRead(HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        alramService.markAllAsRead(loginUser.getUserId());
        return ResponseEntity.ok().build();
    }

}

