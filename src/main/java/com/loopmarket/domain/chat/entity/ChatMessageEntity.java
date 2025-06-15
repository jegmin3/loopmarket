package com.loopmarket.domain.chat.entity;

import lombok.*;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msg_id")
    private Long msgId;

    // FK: 채팅방 ID
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    // FK: 보낸 사람 ID
    @Column(name = "sender_id", nullable = false)
    private Integer senderId;
    
    // 메시지 내용
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    // 읽음 상태
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
    
    // 보낸 시간
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
    
    // 이미지 전송 및 저장용
    @Column(name = "image_url")
    private String imageUrl;

    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}
