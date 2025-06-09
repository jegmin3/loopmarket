package com.loopmarket.domain.alram;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.loopmarket.domain.member.MemberEntity;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AlramController {

    private final AlramService alramService;
    private final AlramRepository alramRepository;
    
    /**
     * 안 읽은 알림 목록 조회
     */
    @GetMapping("/notifications/unread")
    public Map<String, Object> getUnreadAlrams(HttpSession session) {
        Integer userId = getLoginUserId(session);
        List<AlramDTO> list = alramService.getUnreadAlrams(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("count", list.size());
        result.put("notifications", list);
        return result;
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/alram/mark-read")
    public void markAllAsRead(HttpSession session) {
        Integer userId = getLoginUserId(session);
        alramService.markAllAsRead(userId);
    }

    /**
     * 관리자 알림 전송
     */
    @PostMapping("/alram/send")
    public void sendAlram(@RequestBody AlramDTO dto) {
        // 관리자 인증은 생략 (추후 Security로 제한 가능)
        //alramService.createAlram(dto);
        alramService.createOrUpdateChatAlram(dto);
    }
    
    // 알림 하나를 읽음 처리
    @PatchMapping("/alram/{id}/read")
    public void markAsRead(@PathVariable Long id, HttpSession session) {
        Integer loginUserId = getLoginUserId(session);

        // 알림 존재 여부 확인
        AlramEntity alram = alramRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림이 존재하지 않습니다."));

        // 자신에게 온 알림이 아닌 경우 거부
        if (!alram.getUserId().equals(loginUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 알림에 접근할 수 없습니다.");
        }

        // 읽음 처리 후 저장
        alram.setIsRead(true);
        alramRepository.save(alram);
    }

    // 알림 하나 삭제
    @DeleteMapping("/alram/{id}")
    public void deleteAlram(@PathVariable Long id, HttpSession session) {
        Integer loginUserId = getLoginUserId(session);

        // 알림 조회
        AlramEntity alram = alramRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 알림이 없습니다."));

        // 접근 권한 확인
        if (!alram.getUserId().equals(loginUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 알림만 삭제할 수 있습니다.");
        }

        // 삭제 수행
        alramRepository.delete(alram);
    }

    /**
     * 로그인된 사용자 ID 가져오기 (세션 기반)
     */
    private Integer getLoginUserId(HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser != null && loginUser instanceof MemberEntity) {
            MemberEntity member = (MemberEntity) loginUser;
            return member.getUserId(); // users 테이블의 PK
        }
        throw new IllegalStateException("로그인 사용자 아님");
    }

    
}

