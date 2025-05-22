package com.loopmarket.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import com.loopmarket.domain.chat.entity.ChatEntity;

/** 클라이언트와 주고받을 채팅 메시지 데이터 전송 객체 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageDTO {

    private String roomId; // 채팅방 ID
    private Integer senderId; // 보낸 사용자 ID
    private String senderNickname; // 보낸 사용자 닉네임 (뷰 표시용)
    private Integer receiverId; // 받은 사용자 ID
    private String receiverNickname; // 받은 사용자 닉네임 (뷰 표시용)
    private String message; // 메시지 내용
    private LocalDateTime sentAt; // 메시지 전송 시간
    private ChatEntity.MessageType type; // 메시지 타입 (ENTER, TALK, LEAVE 등)
    private Boolean readStatus; // 읽음 여부

    @Builder
    public ChatMessageDTO(String roomId, Integer senderId, String senderNickname,
                          Integer receiverId, String receiverNickname, String message,
                          LocalDateTime sentAt, ChatEntity.MessageType type, Boolean readStatus) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.receiverId = receiverId;
        this.receiverNickname = receiverNickname;
        this.message = message;
        this.sentAt = sentAt;
        this.type = type;
        this.readStatus = readStatus;
    }

    // ChatEntity로부터 DTO를 생성하는 정적 팩토리 메서드 (선택 사항)
    public static ChatMessageDTO from(ChatEntity chatEntity) {
        return ChatMessageDTO.builder()
                .roomId(chatEntity.getRoomId())
                .senderId(chatEntity.getSender().getUserId())
                .senderNickname(chatEntity.getSender().getNickname())
                .receiverId(chatEntity.getReceiver().getUserId())
                .receiverNickname(chatEntity.getReceiver().getNickname())
                .message(chatEntity.getMessage())
                .sentAt(chatEntity.getSentAt())
                .type(chatEntity.getType())
                .readStatus(chatEntity.getReadStatus())
                .build();
    }
}
