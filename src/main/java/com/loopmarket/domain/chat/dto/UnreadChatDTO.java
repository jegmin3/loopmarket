package com.loopmarket.domain.chat.dto;

import java.time.LocalDateTime;

/** chatList에서 메시지 실시간 갱신용 DTO */
public class UnreadChatDTO {
	
    private Long roomId;
    private int unreadCount;
    private String lastMessage;
    private LocalDateTime lastTime;
    
    // 그냥 롬복 안쓰고 만들어 봤습니다.
    public UnreadChatDTO(Long roomId, int unreadCount, String lastMessage, LocalDateTime lastTime) {
        this.roomId = roomId;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
    }

    // 게터세터
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public LocalDateTime getLastTime() { return lastTime; }
    public void setLastTime(LocalDateTime lastTime) { this.lastTime = lastTime; }
}


