package com.loopmarket.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatMessageDTO.MessageType;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.service.ChatService;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * WebSocket 채팅 컨트롤러
 * - 클라이언트의 채팅 메시지 수신
 * - DB 저장 후 구독자에게 전송
 */
@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트로부터 채팅 메시지를 수신
     * - 경로: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatMessageDTO messageDTO) {
        // 1. 메시지 저장
        ChatMessageEntity saved = chatService.saveMessage(
                messageDTO.getRoomId(),
                messageDTO.getSenderId(),
                messageDTO.getContent()
        );

        // 2. 응답 메시지 구성 (timestamp, 읽음 여부 포함)
        ChatMessageDTO dto = ChatMessageDTO.fromEntity(saved);

        // 3. 채팅방 구독자에게 메시지 전송
        messagingTemplate.convertAndSend("/queue/room." + saved.getRoomId(), dto);
    }
    
    /**
     * 클라이언트가 채팅방 입장 후 메시지 읽음 처리 요청
     * - 경로: /app/chat.read
     * - 프론트에서 roomId, userId만 전달
     */
    @MessageMapping("/chat.read")
    public void handleReadMessage(ChatMessageDTO dto) {
    	
    	//chatService.markMessagesAsRead(dto.getRoomId(), dto.getSenderId());
    	
        List<ChatMessageEntity> updatedMessages = chatService.markMessagesAsRead(
                dto.getRoomId(),
                dto.getSenderId()
        );
        //읽음 처리된 메시지를 다시 클라이언트로 전송
        for (ChatMessageEntity msg : updatedMessages) {
            ChatMessageDTO CMDto = ChatMessageDTO.fromEntity(msg);

            // 메시지 보낸 상대방에게도 전송 (방 전체 구독 대상)
           messagingTemplate.convertAndSend("/queue/room." + msg.getRoomId(), CMDto);
        }
    }

}

