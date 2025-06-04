package com.loopmarket.domain.chat.dto;

import java.time.LocalDateTime;

import com.loopmarket.domain.chat.entity.ChatRoomEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomSummaryDTO {
    private ChatRoomEntity room;
    private String opponentNickname;
    
    private String lastMessage;      // 마지막 메시지 내용
    private LocalDateTime lastTime;  // 마지막 메시지 시간
}

