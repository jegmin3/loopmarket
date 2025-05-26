package com.loopmarket.domain.chat.controller;

import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatRoomDTO;
import com.loopmarket.domain.chat.service.ChatService;
import com.loopmarket.domain.member.MemberEntity; // 세션에서 사용자 정보 가져오기 위함
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession; // 세션 접근
import java.util.List;

/** 채팅 관련 RESTful API를 제공하는 컨트롤러 */
@RestController // RESTful API 컨트롤러
@RequestMapping("/api/chat") // 기본 URL 경로
@RequiredArgsConstructor
public class ChatApiController {

    private final ChatService chatService;

    /**
     * 현재 로그인한 사용자의 모든 채팅방 목록을 조회합니다.
     *
     * @param session HTTP 세션 (로그인 사용자 정보 획득)
     * @return 채팅방 DTO 목록
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getChatRooms(HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 로그인되지 않은 사용자 처리
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        List<ChatRoomDTO> chatRooms = chatService.getChatRoomsForUser(loginUser.getUserId());
        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 특정 채팅방의 메시지 기록을 조회합니다. (이전 대화 내역 불러오기)
     *
     * @param roomId 조회할 채팅방 ID
     * @param session HTTP 세션 (사용자 권한 확인 등)
     * @return 메시지 DTO 목록
     */
    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable String roomId, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        // TODO: (선택 사항) 실제 서비스에서는 roomId에 해당하는 채팅방에 loginUser가 접근 권한이 있는지 확인하는 로직이 필요합니다.
        // 예를 들어, chatRoomRepository.findByRoomId(roomId)를 통해 채팅방을 조회한 후
        // loginUser.getUserId()가 user1 또는 user2에 해당하는지 검증해야 합니다.

        List<ChatMessageDTO> messages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 특정 채팅방의 모든 메시지를 '읽음' 상태로 업데이트합니다.
     * 클라이언트에서 채팅방에 진입할 때 호출될 수 있습니다.
     *
     * @param roomId 메시지를 읽음 처리할 채팅방 ID
     * @param session HTTP 세션 (로그인 사용자 정보 획득)
     * @return 성공 여부
     */
    @PostMapping("/messages/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable String roomId, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
        	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        // TODO: (선택 사항) 여기도 접근 권한 확인 로직 추가 필요

        chatService.markMessagesAsRead(roomId, loginUser.getUserId());
        return ResponseEntity.ok().build(); // 성공 응답
    }
    
    /**
     * 특정 채팅방과 관련된 모든 메시지 및 채팅방 엔티티를 삭제합니다.
     *
     * @param roomId 삭제할 채팅방 ID
     * @param session HTTP 세션 (권한 확인)
     * @return 성공 여부
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteChatRoomAndMessages(@PathVariable String roomId, HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
        }

        // TODO: 로그인 사용자가 해당 채팅방의 참여자인지 확인하는 로직 추가
        // chatService.isUserInChatRoom(roomId, loginUser.getUserId()); 와 같은 메서드 필요

        try {
            chatService.deleteChatRoomAndMessages(roomId);
            return ResponseEntity.ok().build(); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found (채팅방 없음)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
    
}
