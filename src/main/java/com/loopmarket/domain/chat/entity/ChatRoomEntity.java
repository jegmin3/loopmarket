package com.loopmarket.domain.chat.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/** 1:1 채팅방 정보를 담는 엔티티 */
@Entity
@Table(name = "chat_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    // 회원 ID 1 (FK: users.user_id)
    @Column(name = "user1_id", nullable = false)
    private Integer user1Id;

    // 회원 ID 2 (FK: users.user_id)
    @Column(name = "user2_id", nullable = false)
    private Integer user2Id;

    @Builder.Default
    @Column(name = "user1_leaved", nullable = false)
    private boolean user1Leaved = false;

    @Builder.Default
    @Column(name = "user2_leaved", nullable = false)
    private boolean user2Leaved = false;
    
    @Column(name = "product_id", nullable = false)
    private Long productId; // 어떤 상품에 대한 채팅방인지
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

