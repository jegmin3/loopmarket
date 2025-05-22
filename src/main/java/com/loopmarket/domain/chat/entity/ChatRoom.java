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
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;
    
    // ManyToOne 연결 고려 (아마 안하고 그냥 Integer 그대로 둘듯)
    @Column(name = "user1_id")
    private Integer user1Id;
    @Column(name = "user2_id")
    private Integer user2Id;

    
    @Builder.Default
    @Column(name = "user1_exit")
    private Boolean user1Exit = false;
    @Builder.Default
    @Column(name = "user2_exit")
    private Boolean user2Exit = false;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;
    
    @Column(name = "user1_unread_count")
    private Integer user1UnreadCount;
    @Column(name = "user2_unread_count")
    private Integer user2UnreadCount;

    private LocalDateTime createdAt;
    
    /** PrePersist : creatAt 시간 자동 등록용 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

