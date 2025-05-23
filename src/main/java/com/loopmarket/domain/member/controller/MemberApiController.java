package com.loopmarket.domain.member.controller;

import com.loopmarket.domain.member.MemberService; // MemberService 주입
import com.loopmarket.domain.member.MemberEntity; // 세션에서 사용자 정보 가져오기 위함
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/** 회원 관련 RESTful API를 제공하는 컨트롤러 */
@RestController // RESTful API 컨트롤러
@RequestMapping("/api/member") // 기본 URL 경로
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /**
     * 이메일로 사용자 ID와 닉네임을 조회합니다. (새 채팅 시작 시 상대방 찾기 용도)
     *
     * @param email 조회할 사용자 이메일
     * @return 사용자 ID와 닉네임을 포함하는 JSON 응답, 또는 404 Not Found
     */
    @GetMapping("/id-by-email")
    public ResponseEntity<?> getMemberIdByEmail(@RequestParam String email) {
        MemberEntity member = memberService.findMemberByEmail(email);
        if (member != null) {
            // DTO로 반환하는 것이 더 좋지만, chatRoom.html에서 Map 형태를 기대하므로 Map 사용
            // 실제 서비스에서는 MemberResponseDTO와 같은 DTO를 만들어 사용하는 것이 좋습니다.
            return ResponseEntity.ok(Map.of(
                    "userId", member.getUserId(),
                    "nickname", member.getNickname(),
                    "email", member.getEmail() // 이메일도 반환 (선택 사항)
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email); // 404 Not Found
        }
    }

    /**
     * 특정 사용자의 FCM 토큰을 업데이트합니다.
     *
     * @param userId 경로 변수로 받은 사용자 ID
     * @param payload FCM 토큰을 포함하는 요청 본문 (JSON: { "fcmToken": "..." })
     * @param session HTTP 세션 (로그인 사용자 검증용)
     * @return 성공 여부
     */
    @PostMapping("/{userId}/fcm-token")
    public ResponseEntity<Void> updateFcmToken(@PathVariable Integer userId, @RequestBody Map<String, String> payload, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        // 1. 로그인 여부 및 권한 확인
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }
        // 2. 요청 userId와 로그인 사용자 ID가 일치하는지 확인 (자기 자신의 토큰만 업데이트 가능)
        if (!loginUser.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        String fcmToken = payload.get("fcmToken");
        if (fcmToken == null || fcmToken.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        try {
            memberService.updateFcmToken(userId, fcmToken);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (IllegalArgumentException e) {
            // userId에 해당하는 멤버를 찾을 수 없을 경우 (memberService에서 던지는 예외)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found
        } catch (Exception e) {
            // 그 외 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }

    // TODO: (선택 사항) 알림 뱃지 업데이트를 위한 API (header.html에서 사용)
    // 현재 header.html에 "/api/notifications/unread"가 있습니다.
    // 이 API는 알림 엔티티/서비스가 별도로 있다면 해당 컨트롤러에 구현하는 것이 좋습니다.
    // 임시로 이곳에 추가하거나, 별도의 NotificationApiController를 만들 수 있습니다.
}
