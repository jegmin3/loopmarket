package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    /**
     * 채팅방 내 모든 메시지를 시간순으로 조회
     * - 채팅방 입장 시 메시지 로딩에 사용
     */
    List<ChatMessageEntity> findByRoomIdOrderBySentAtAsc(Long roomId);

    /**
     * 채팅방 내에서 읽지 않은 메시지 목록 조회
     * - 읽음 표시, 알림 등에 활용
     */
    List<ChatMessageEntity> findByRoomIdAndIsReadFalse(Long roomId);

    /**
     * 채팅방 나가기 시 메시지 전체 삭제
     * - ON DELETE CASCADE가 있더라도 수동 삭제 로직이 필요할 경우
     */
    void deleteByRoomId(Long roomId);
    
    // 해당 채팅방에서 가장 최근 메시지 1개
    Optional<ChatMessageEntity> findTopByRoomIdOrderBySentAtDesc(Long roomId);
    
    // 해당 채팅방에서 내가 아닌 사람이 보냈고, 아직 읽지 않은 메시지 개수
    int countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Integer senderId);
    

    
}

