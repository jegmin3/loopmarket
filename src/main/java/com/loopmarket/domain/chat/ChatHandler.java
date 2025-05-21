package com.loopmarket.domain.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopmarket.domain.chat.entity.ChatMessage;
import com.loopmarket.domain.chat.entity.ChatMessage.MessageType;
import com.loopmarket.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 채팅 웹소켓 핸들러. 클라이언트가 메시지를 보내면
 *  1. JSON → ChatMessage 객체로 파싱 
 *  2. DB에 메시지 저장 (chatService.saveMessage)
 *  3. 채팅방 마지막 메시지/시간/안읽은 개수 갱신
 *  같은 방의 다른 사용자에게 메시지 전송 (WebSocket)
 *  */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {
	
	private final ChatService chatService;
	private final ObjectMapper objectMapper;
	
    // 방ID -> 세션목록 (채팅방 별로 현재 접속중인 세션을 저장함)
    private final Map<String, Set<WebSocketSession>> roomSessions = new HashMap<>();
    
    /** 웹소켓 연결시 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomIdFromUri(session);
        log.info("웹소켓 연결됨 - roomId: {}, sessionId: {}", roomId, session.getId());
        // roomId나 유저 정보 추출 및 세션 관리

        // rooId가 없으면 새로 생성해서 추가
        roomSessions.computeIfAbsent(roomId, k -> new HashSet<>()).add(session);
    }
    
    /** 텍스트 메시지 전송시 */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = getRoomIdFromUri(session);
        ChatMessageDTO dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);

        log.info("[WS 수신] [{}] {} → {}: {}", roomId, dto.getSender(), dto.getReceiver(), dto.getContent());

        // DTO → Entity 변환
        // 복잡도가 낮아서 굳이 mapper클래스를 만들고 분리하지 않았음
        ChatMessage entity = ChatMessage.builder()
                .roomId(Long.parseLong(dto.getRoomId()))
                .senderId(Integer.parseInt(dto.getSender()))
                .receiverId(Integer.parseInt(dto.getReceiver()))
                .content(dto.getContent())
                .messageType(MessageType.valueOf(Optional.ofNullable(dto.getMessageType()).orElse("TEXT")))
                .build();
        
        // DB에 저장
        chatService.saveMessage(entity);

        // 그대로 브로드캐스트
        String json = objectMapper.writeValueAsString(dto);
        for (WebSocketSession s : roomSessions.getOrDefault(roomId, Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }
    
    /** 
     * 연결이 끊기면 방에서 제거하고,
     * 아무도 없으면 map에서도 제거함 
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomIdFromUri(session);
        log.info("연결 종료 - roomId: {}, sessionId: {}", roomId, session.getId());

        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomId);
            }
        }
    }

    // URI에서 roomId 추출 (-> /ws/chat/{roomId})
    private String getRoomIdFromUri(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
    
    
    
}

