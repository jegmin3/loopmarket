package com.loopmarket.domain.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.loopmarket.domain.alram.AlramDTO;
import com.loopmarket.domain.alram.AlramService;
import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatMessageDTO.MessageType;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;
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
    private final ChatRoomRepository chatRoomRepository;
    private final AlramService alramService;

    /**
     * 클라이언트로부터 채팅 메시지를 수신
     * - 경로: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(ChatMessageDTO messageDTO) {
        // 메시지 저장
        ChatMessageEntity saved = chatService.saveMessage(
                messageDTO.getRoomId(),
                messageDTO.getSenderId(),
                messageDTO.getContent()
        );

        // 응답 메시지 구성 (timestamp, 읽음 여부 포함)
        ChatMessageDTO dto = ChatMessageDTO.fromEntity(saved, MessageType.CHAT);
        
        // --------------알림용 로직-----------------
        // 1. 채팅방 정보 가져오기
        ChatRoomEntity room = chatRoomRepository.findById(saved.getRoomId())
            .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 2. 상대방 ID 구하기
        Integer receiverId = (room.getUser1Id().equals(saved.getSenderId()))
            ? room.getUser2Id()
            : room.getUser1Id();

        // 3. 상대방이 채팅방에 없으면 알림 전송
        //if (!chatSessionTracker.isUserInRoom(saved.getRoomId(), receiverId)) {
        	System.out.println("알림 생성 시도!");
            alramService.createOrUpdateChatAlram(
                AlramDTO.builder()
                    .userId(receiverId)
                    .senderId(saved.getSenderId())
                    .type("CHAT")
                    .title("새 채팅 도착")
                    .content("상대방이 메시지를 보냈어요.")
                    .url("/chat/room/" + saved.getRoomId())
                    .build()
            );
        //} else {
        //	System.out.println("알림 전송되지 않음! 상대방이 채팅방에 있습니다.");
        //}
        // ----------------알림용 로직 끝-----------------
        
        // 채팅방 구독자에게 메시지 전송(브로드캐스트)
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

