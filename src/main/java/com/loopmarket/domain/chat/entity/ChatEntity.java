package com.loopmarket.domain.chat.entity;

import com.loopmarket.domain.member.MemberEntity; // MemberEntity 임포트
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/** 1:1 채팅 메시지 정보를 담는 엔티티 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_messages") // 테이블명은 chat_messages로 명시
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 ID (두 사용자 ID를 조합하여 생성, 예: user1Id_user2Id)
    @Column(nullable = false, length = 100)
    private String roomId;

    // 메시지를 보낸 사용자
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "sender_id", nullable = false) // FK 컬럼명
    private MemberEntity sender; // MemberEntity 객체로 저장

    // 메시지를 받은 사용자 (1:1 채팅의 상대방)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private MemberEntity receiver; // MemberEntity 객체로 저장

    @Column(nullable = false, columnDefinition = "TEXT") // 메시지 내용은 TEXT 타입으로 충분히 크게
    private String message;

    @Column(nullable = false)
    private LocalDateTime sentAt; // 메시지 전송 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type; // 메시지 타입 (ENTER, TALK, LEAVE)

    @Column(nullable = false)
    private Boolean readStatus = false; // 상대방의 읽음 여부 (true: 읽음, false: 안 읽음)

    // 빌더 패턴을 위한 생성자
    @Builder
    public ChatEntity(String roomId, MemberEntity sender, MemberEntity receiver, String message, MessageType type, Boolean readStatus) {
        this.roomId = roomId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.type = type;
        this.sentAt = LocalDateTime.now(); // 빌더 사용 시 현재 시간 자동 설정
        this.readStatus = (readStatus != null) ? readStatus : false; // 기본값 false
    }

    public enum MessageType {
        ENTER, TALK, LEAVE, IMAGE, FILE // 채팅 메시지 타입 확장 가능
    }
}