package com.loopmarket.domain.chat.dto;

import com.loopmarket.domain.chat.entity.ChatRoomEntity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatRoomSummaryDTO {
    private ChatRoomEntity room;
    private String opponentNickname;
}

