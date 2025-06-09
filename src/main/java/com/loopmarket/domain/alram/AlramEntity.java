package com.loopmarket.domain.alram;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alram")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alram_id")
    private Long alramId; // 알림 ID (PK)

    @Column(name = "user_id", nullable = false)
    private Integer userId; // 알림 수신자 ID

    @Column(name = "sender_id")
    private Integer senderId; // 알림 발신자 ID (nullable)

    @Column(name = "type", length = 20, nullable = false)
    private String type; // 알림 유형 (예: CHAT, ADMIN 등)

    @Column(name = "title", length = 100, nullable = false)
    private String title; // 알림 제목

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 알림 본문

    @Column(name = "url", length = 255)
    private String url; // 클릭 시 이동할 경로

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false; // 읽음 여부 (기본값 false)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 생성 시각

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 자동 시간 기록 (g현재시간)
    }
}

