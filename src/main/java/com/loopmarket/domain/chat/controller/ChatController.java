package com.loopmarket.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping; // STOMP 메시지 매핑
import org.springframework.messaging.handler.annotation.Payload; // 메시지 페이로드 주입
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.service.ChatService;

/** 웹소켓(STOMP) 메시지를 처리하는 컨트롤러 */
@Controller
@RequiredArgsConstructor
public class ChatController extends BaseController {

    private final ChatService chatService;

    /**
     * 클라이언트가 "/pub/chat/message" 경로로 메시지를 발행할 때 처리합니다.
     * 메시지 전송, 채팅방 입장/퇴장 등 모든 채팅 관련 메시지를 처리합니다.
     *
     * @param chatMessageDTO 클라이언트로부터 수신한 채팅 메시지 DTO
     */
    @MessageMapping("/chat/message") // /pub/chat/message 에 매핑 (WebSocketConfig에서 설정)
    public void message(@Payload ChatMessageDTO chatMessageDTO) {
        // ChatService를 통해 메시지 처리 (저장, 웹소켓 브로드캐스팅, FCM 알림 등)
        chatService.handleChatMessage(chatMessageDTO);
    }
    
    @GetMapping("/chat/page")
    public String goChatPage(Model model, RedirectAttributes redirectAttributes) {
    	redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
    	return render("chat/chatRoom", model);
    }

    // TODO: (선택 사항) 웹소켓 연결/연결 해제 이벤트를 처리하는 로직 추가 가능
    // SimpAnnotationMethodMessageHandler 에서 WebSocketEventListener 등을 사용하여
    // connect, disconnect 이벤트를 감지하여 사용자 온라인 상태 관리 로직을 구현할 수 있습니다.
    // 이는 ChatService의 isReceiverOnline 로직과 연동될 수 있습니다.
}
