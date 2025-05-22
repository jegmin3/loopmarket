package com.loopmarket.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopmarket.domain.chat.entity.ChatRoomEntity;

import java.util.List;
import java.util.Optional;

/** 채팅방(ChatRoomEntity) 데이터베이스 접근 리포지토리 */
public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, String> {

    // roomId로 채팅방 조회 (기본 JpaRepository의 findById와 동일하지만 명시적으로)
    Optional<ChatRoomEntity> findByRoomId(String roomId);

    // 특정 두 사용자 ID로 채팅방 조회
    // 1:1 채팅에서 두 사용자의 순서에 관계없이 채팅방을 찾기 위함
    Optional<ChatRoomEntity> findByUser1_UserIdAndUser2_UserId(Integer user1Id, Integer user2Id);
    Optional<ChatRoomEntity> findByUser2_UserIdAndUser1_UserId(Integer user2Id, Integer user1Id);

    // 특정 사용자가 참여하고 있는 모든 채팅방 목록 조회
    // (이 쿼리는 user1 또는 user2로 있는 모든 채팅방을 찾습니다.)
    List<ChatRoomEntity> findByUser1_UserIdOrUser2_UserIdOrderByLastMessageTimeDesc(Integer userId1, Integer userId2);
}
