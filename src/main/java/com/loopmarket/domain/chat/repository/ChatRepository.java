package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopmarket.domain.chat.entity.ChatEntity;

import java.time.LocalDateTime;
import java.util.List;

/** 채팅 메시지(ChatEntity) 데이터베이스 접근 리포지토리 */
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {

    // 특정 채팅방의 메시지를 시간 순서대로 조회
    List<ChatEntity> findByRoomIdOrderBySentAtAsc(String roomId);

    // 특정 채팅방에서 특정 시간 이후의 메시지를 조회 (예: 새로고침 시)
    List<ChatEntity> findByRoomIdAndSentAtAfterOrderBySentAtAsc(String roomId, LocalDateTime sentAt);

    // 특정 채팅방에서 특정 사용자가 읽지 않은 메시지 수 조회
    // receiver_id가 현재 사용자의 ID이고, readStatus가 false인 메시지 카운트
    long countByRoomIdAndReceiver_UserIdAndReadStatusFalse(String roomId, Integer receiverUserId);
    
    // 특정 채팅방에서 특정 사용자가 받은 메시지 중 읽지 않은 메시지 목록 조회 (채팅방 목록 미리보기에 활용)
    List<ChatEntity> findByRoomIdAndReceiver_UserIdAndReadStatusFalse(String roomId, Integer receiverUserId); // 이 메서드를 추가합니다.

    // 보통 ChatRoomEntity에 lastMessagePreview를 저장하는 방식으로 최적화하므로,
    // 이 쿼리는 빈번하게 사용하지 않을 수 있습니다.
}
