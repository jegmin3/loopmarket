package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.loopmarket.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 두 유저 간의 채팅방 조회 (순서 무관)
    Optional<ChatRoom> findByUser1IdAndUser2Id(Integer user1Id, Integer user2Id);
    Optional<ChatRoom> findByUser2IdAndUser1Id(Integer user1Id, Integer user2Id);

    // 특정 유저가 참여한 채팅방 모두 조회
    List<ChatRoom> findByUser1IdOrUser2Id(Integer userId1, Integer userId2);
}

