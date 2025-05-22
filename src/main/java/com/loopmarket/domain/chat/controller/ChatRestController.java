package com.loopmarket.domain.chat.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.chat.ChatMessageDTO;
import com.loopmarket.domain.chat.entity.ChatMessage;
import com.loopmarket.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    
    //WebSocket이 실패했을 때 Fallback 저장
    @PostMapping("/message")
    public ResponseEntity<String> saveMessage(@RequestBody ChatMessageDTO dto) {
        ChatMessage message = ChatMessage.builder()
                .roomId(Long.parseLong(dto.getRoomId()))
                .senderId(Integer.parseInt(dto.getSender()))
                .receiverId(Integer.parseInt(dto.getReceiver()))
                .content(dto.getContent())
                .messageType(ChatMessage.MessageType.valueOf(
                        Optional.ofNullable(dto.getMessageType()).orElse("TEXT")))
                .build();

        chatService.saveMessage(message);
        return ResponseEntity.ok("메시지 저장 완료");
    }
}

