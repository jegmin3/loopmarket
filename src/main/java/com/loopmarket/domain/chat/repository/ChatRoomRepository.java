package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopmarket.domain.chat.entity.ChatRoomEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    /**
     * 두 사용자가 참여한 채팅방을 조회
     * - 두 사용자의 조합으로 유일한 채팅방 존재
     */
    Optional<ChatRoomEntity> findByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);

    /**
     * 현재 사용자가 참여하고 있는 채팅방 전체 조회
     * - 마이페이지: 나의 채팅방 목록 조회 시 사용
     */
    List<ChatRoomEntity> findByUser1IdOrUser2Id(Integer userId1, Integer userId2);
}
