package com.loopmarket.domain.chat.entity;

import lombok.*;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    private Long roomId;

    private Integer senderId;
    private Integer receiverId;

    @Column(columnDefinition = "TEXT")
    private String content;
    
    /** EnumType.String을 사용하여 DB에 TEXT, IMAGE, SYSTEM 그대로 저장되게 */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;
    
    @Builder.Default
    private Boolean isRead = false;
    private LocalDateTime readAt;
    
    @Builder.Default
    private Boolean delivered = false;
    
    @Builder.Default
    private Boolean deletedBySender = false;
    @Builder.Default
    private Boolean deletedByReceiver = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    public enum MessageType {
        TEXT, IMAGE, SYSTEM
    }
}
