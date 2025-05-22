package com.loopmarket.domain.chat;

import lombok.Data;

@Data
public class ChatMessageDTO {
	// 송신자 (사용자 명 = userId)
    private String sender;
    // 수신자
    private String receiver;
    // 방 ID
    private String roomId;
    // 내용
    private String content;
    // 메시지 타입 (Text등)
    private String messageType;
}
