package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopmarket.domain.chat.entity.ChatMessage;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 메시지 전체 조회 (정렬)
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // 안 읽은 메시지 개수
    int countByRoomIdAndReceiverIdAndIsReadFalse(Long roomId, Integer receiverId);

    // 가장 최근 메시지
    ChatMessage findTopByRoomIdOrderByCreatedAtDesc(Long roomId);
}

