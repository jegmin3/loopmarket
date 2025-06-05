package com.loopmarket.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatMessageDTO.MessageType;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.service.ChatService;
import com.loopmarket.domain.chat.util.ChatSessionTracker;

import java.security.Principal;

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
    private final ChatSessionTracker chatSessionTracker;

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
        ChatMessageDTO dto = ChatMessageDTO.fromEntity(saved, MessageType.CHAT);

        // 3. 채팅방 구독자에게 메시지 전송
        messagingTemplate.convertAndSend("/queue/room." + saved.getRoomId(), dto);
    }
    
    /**
     * 클라이언트가 채팅방 입장 후 메시지 읽음 처리 요청
     * - 경로: /app/chat.read
     * - 프론트에서 roomId, userId만 전달
     */
    @MessageMapping("/chat.read")
    public void handleReadMessage(@Payload ChatMessageDTO dto, Principal principal) {
        if (principal == null) {
            System.out.println("principal이 null입니다. WebSocket 인증 누락.");
            return;
        }
        
        Integer viewerId = Integer.parseInt(principal.getName()); // 무조건 내 ID
        Long roomId = dto.getRoomId(); // 채팅방 ID
        
        // 연결됐는지 세션 추적용 (내가 방에 접속했다는 정보 등록)
        chatSessionTracker.userEnterRoom(roomId, viewerId);
        // 상대방이 보낸 안읽은 메시지를 읽음처리만 하고 메시지 안보냄
		chatService.markMessagesAsRead(dto.getRoomId(), dto.getSenderId());
		
	    // 상대방에게 읽음 알림 전송
	    ChatRoomEntity room = chatService.getChatRoomById(roomId);
	    if (room == null) return;
	    
	    // senderId(나)가 user1 이면 
	    Integer partnerId = room.getUser1Id().equals(viewerId) 
	    		? room.getUser2Id() 
	    		: room.getUser1Id();
	    
	    // 읽음 알림 구성
	    ChatMessageDTO readMsg = new ChatMessageDTO();
	    readMsg.setType(MessageType.READ);
	    readMsg.setRoomId(roomId);
	    readMsg.setSenderId(viewerId);
	    
	    System.out.println("읽음 메시지 전송 → " + partnerId + " / room: " + roomId);
	    
	    // 상대방에게 읽음 메시지 전송
//	    messagingTemplate.convertAndSendToUser(
//	    	partnerId.toString(),
//	        "/queue/room." + roomId,
//	        readMsg
//	    );
	    // 유저별로 보내지 않음 (방 전체에 브로드캐스트)
	    messagingTemplate.convertAndSend("/queue/room." + roomId, readMsg);


    }

}

