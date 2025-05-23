package com.loopmarket.domain.chat.entity;

import com.loopmarket.domain.member.MemberEntity; // MemberEntity 임포트
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;

/** 1:1 채팅방 정보를 담는 엔티티 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_rooms") // 테이블명은 chat_rooms로 명시
public class ChatRoomEntity {

    // 1:1 채팅의 경우 roomId를 두 사용자 ID의 조합으로 설정하여 고유하게 사용
    @Id
    @Column(nullable = false, unique = true, length = 100)
    private String roomId; // 예: "user1_user2" (항상 작은 ID가 먼저 오도록 정렬)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private MemberEntity user1; // 채팅방에 참여한 첫 번째 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private MemberEntity user2; // 채팅방에 참여한 두 번째 사용자

    @Column(nullable = false)
    private LocalDateTime createdAt; // 채팅방 생성 시간

    // (선택 사항) 마지막 메시지 관련 정보 - 최신 메시지 조회 시 성능 최적화에 도움
    // 직접 ChatMessage를 참조하기보다, String으로 저장하여 불필요한 조인 방지
    @Column(length = 500)
    private String lastMessagePreview; // 채팅방 마지막 메시지 미리보기

    private LocalDateTime lastMessageTime; // 마지막 메시지 전송 시간

    @Builder
    public ChatRoomEntity(String roomId, MemberEntity user1, MemberEntity user2,
                          String lastMessagePreview, LocalDateTime lastMessageTime) {
        this.roomId = roomId;
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = LocalDateTime.now(); // 생성 시점 자동 설정
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageTime = lastMessageTime;
    }

    // 두 사용자 ID를 기반으로 일관된 roomId를 생성하는 정적 메서드
    public static String generateRoomId(Integer user1Id, Integer user2Id) {
        if (user1Id < user2Id) {
            return user1Id + "_" + user2Id;
        } else {
            return user2Id + "_" + user1Id;
        }
    }
}
