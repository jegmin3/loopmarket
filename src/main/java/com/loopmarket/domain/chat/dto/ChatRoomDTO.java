package com.loopmarket.domain.chat.dto;

import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.member.MemberEntity; // MemberEntity 임포트 (from 메서드 내에서 사용)
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/** 채팅방 정보를 나타내는 데이터 전송 객체 (ChatRoomEntity 반영) */
@Getter
@Setter
@NoArgsConstructor
public class ChatRoomDTO {

    private String roomId; // 채팅방 고유 ID (예: user1Id_user2Id)
    private Integer user1Id; // 채팅방에 참여한 첫 번째 사용자 ID
    private String user1Nickname; // 채팅방에 참여한 첫 번째 사용자 닉네임
    private Integer user2Id; // 채팅방에 참여한 두 번째 사용자 ID
    private String user2Nickname; // 채팅방에 참여한 두 번째 사용자 닉네임
    private String lastMessagePreview; // 채팅방의 마지막 메시지 미리보기
    private LocalDateTime lastMessageTime; // 마지막 메시지 전송 시간
    private Integer unreadMessageCount; // 현재 사용자의 읽지 않은 메시지 수 (이 필드는 서비스 로직에서 채워야 함)

    @Builder
    public ChatRoomDTO(String roomId, Integer user1Id, String user1Nickname,
                       Integer user2Id, String user2Nickname, String lastMessagePreview,
                       LocalDateTime lastMessageTime, Integer unreadMessageCount) {
        this.roomId = roomId;
        this.user1Id = user1Id;
        this.user1Nickname = user1Nickname;
        this.user2Id = user2Id;
        this.user2Nickname = user2Nickname;
        this.lastMessagePreview = lastMessagePreview;
        this.lastMessageTime = lastMessageTime;
        this.unreadMessageCount = unreadMessageCount;
    }

    // ChatRoomEntity와 현재 로그인한 사용자를 기반으로 DTO를 생성하는 정적 팩토리 메서드
    public static ChatRoomDTO from(ChatRoomEntity chatRoomEntity, Integer currentUserId) {
        // 현재 로그인한 사용자가 user1인지 user2인지 판단하여, 상대방 정보를 DTO에 담기
        MemberEntity user1 = chatRoomEntity.getUser1();
        MemberEntity user2 = chatRoomEntity.getUser2();

        Integer otherUserId;
        String otherUserNickname;

        if (user1.getUserId().equals(currentUserId)) {
            otherUserId = user2.getUserId();
            otherUserNickname = user2.getNickname();
        } else {
            otherUserId = user1.getUserId();
            otherUserNickname = user1.getNickname();
        }

        return ChatRoomDTO.builder()
                .roomId(chatRoomEntity.getRoomId())
                .user1Id(user1.getUserId()) // user1, user2는 채팅방 고유 멤버
                .user1Nickname(user1.getNickname())
                .user2Id(user2.getUserId())
                .user2Nickname(user2.getNickname())
                .lastMessagePreview(chatRoomEntity.getLastMessagePreview())
                .lastMessageTime(chatRoomEntity.getLastMessageTime())
                // unreadMessageCount는 서비스 로직에서 별도로 계산하여 설정해야 합니다.
                // 여기서는 초기값이거나 null로 두거나 0으로 설정합니다.
                .unreadMessageCount(0) // 초기값 또는 미설정
                .build();
    }
}