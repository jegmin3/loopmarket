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
    private int unreadCount;		 // 안읽은 채팅메시지 개수
    private boolean lastMine; 		 // 내가 보낸 마지막 메시지(읽음/안읽음 처리용)
    private String profileImagePath; // 상대방 프로필 이미지용
    
    private Long productId;		  // 상품 아이디
    private String pdTitle;		  // 상품 제목
    private String ThumbnailPath; // 상품 사진
    
}

