package com.loopmarket.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopmarket.domain.chat.dto.ChatRoomSummaryDTO;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.repository.ChatMessageRepository;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;
import com.loopmarket.domain.member.MemberRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    /**
     * 두 유저 간의 채팅방을 찾거나, 없으면 새로 생성해서 반환
     */
    @Transactional
    public ChatRoomEntity enterRoom(Integer user1Id, Integer user2Id) {
        // 기존 채팅방 존재 확인 (user1-user2, user2-user1 양방향 체크)
        return chatRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
                .or(() -> chatRoomRepository.findByUser1IdAndUser2Id(user2Id, user1Id))
                .orElseGet(() -> {
                    // 새 채팅방 생성
                    ChatRoomEntity newRoom = ChatRoomEntity.builder()
                            .user1Id(user1Id)
                            .user2Id(user2Id)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }

    /**
     * 특정 사용자가 참여한 모든 채팅방 목록 조회
     */
    public List<ChatRoomEntity> getUserChatRooms(Integer userId) {
        return chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);
    }
    
    /** 사용자 ID로 닉네임 조회 */
    public String getNicknameByUserId(Integer userId) {
        return memberRepository.findNicknameByUserId(userId)
                .orElse("알 수 없음");
    }

    /**
     * 채팅 메시지 저장
     */
    @Transactional
    public ChatMessageEntity saveMessage(Long roomId, Integer senderId, String content) {
        ChatMessageEntity message = ChatMessageEntity.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content(content)
                .isRead(false)
                .build();

        return chatMessageRepository.save(message);
    }

    /**
     * 특정 채팅방의 모든 메시지 조회 (시간순 정렬)
     */
    public List<ChatMessageEntity> getMessages(Long roomId) {
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    /**
     * 특정 채팅방 내에서, 특정 유저가 안 읽은 메시지를 읽음 처리
     * @return 
     */
    @Transactional
    public List<ChatMessageEntity> markMessagesAsRead(Long roomId, Integer userId) {
        List<ChatMessageEntity> unreadMessages = chatMessageRepository.findByRoomIdAndIsReadFalse(roomId);

        for (ChatMessageEntity msg : unreadMessages) {
            // 본인이 보낸 메시지는 건너뜀
            if (!msg.getSenderId().equals(userId)) {
                msg.setRead(false);
            }
        }
        // 변경된 메시지 목록 반환 (flush는 트랜잭션 끝날 때 반영)
		return unreadMessages;
    }

    /**
     * 사용자가 채팅방을 나갈 때 호출
     * - 두 사용자 모두 나간 경우, 메시지 및 방 삭제
     */
    @Transactional
    public void leaveRoom(Long roomId, Integer userId) {
        ChatRoomEntity room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        if (userId.equals(room.getUser1Id())) {
            room.setUser1Leaved(true);
        } else if (userId.equals(room.getUser2Id())) {
            room.setUser2Leaved(true);
        }

        // 두 유저 모두 나갔다면 메시지 + 채팅방 삭제
        if (room.isUser1Leaved() && room.isUser2Leaved()) {
            chatMessageRepository.deleteByRoomId(roomId);
            chatRoomRepository.delete(room);
        }
    }
    
    /** 채팅방 목록에서 나간방 제외 필터링 */
    public List<ChatRoomEntity> getActiveChatRooms(Integer userId) {
        List<ChatRoomEntity> allRooms = chatRoomRepository.findByUser1IdOrUser2Id(userId, userId);

        // 내가 아직 나가지 않은 방만 반환
        return allRooms.stream()
                .filter(room ->
                    (room.getUser1Id().equals(userId) && !room.isUser1Leaved()) ||
                    (room.getUser2Id().equals(userId) && !room.isUser2Leaved())
                )
                .collect(Collectors.toList()); // toList는 java15이하에서 호환 안되는건가?
    }
    
    public ChatRoomEntity getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new NoSuchElementException("채팅방을 찾을 수 없습니다."));
    }
    
    public List<ChatRoomSummaryDTO> getChatRoomSummaries(Integer userId) {
        List<ChatRoomEntity> rooms = getActiveChatRooms(userId);

        return rooms.stream().map(room -> {
            Integer opponentId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
            String nickname = getNicknameByUserId(opponentId);
            return new ChatRoomSummaryDTO(room, nickname);
        }).collect(Collectors.toList());
    }

    
    
}

