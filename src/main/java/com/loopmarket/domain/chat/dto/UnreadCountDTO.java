package com.loopmarket.domain.chat.dto;

public class UnreadCountDTO {
    private Long roomId;
    private int count;
    
    // 롬복 안쓰고 만들어 봄
    public UnreadCountDTO(Long roomId, int count) {
        this.roomId = roomId;
        this.count = count;
    }

    // Getter/Setter
    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

