package com.loopmarket.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.loopmarket.domain.chat.entity.ChatMessage;
import com.loopmarket.domain.chat.entity.ChatRoom;
import com.loopmarket.domain.chat.repository.ChatMessageRepository;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepo;
    private final ChatMessageRepository chatMessageRepo;

    @Override
    public ChatRoom createOrGetRoom(Integer user1Id, Integer user2Id) {
        // 항상 작은 ID가 user1으로 가게 정렬
        int min = Math.min(user1Id, user2Id);
        int max = Math.max(user1Id, user2Id);

        return chatRoomRepo.findByUser1IdAndUser2Id(min, max)
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .user1Id(min)
                            .user2Id(max)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return chatRoomRepo.save(room);
                });
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message) {
        // 1. 메시지 저장
        ChatMessage saved = chatMessageRepo.save(message);

        // 2. 채팅방 조회
        ChatRoom room = chatRoomRepo.findById(message.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        // 3. 마지막 메시지 / 시간 갱신
        room.setLastMessage(message.getContent());
        room.setLastMessageTime(message.getCreatedAt());

        // 4. 수신자 기준으로 안읽은 메시지 개수 +1
        if (Objects.equals(room.getUser1Id(), message.getReceiverId())) {
            room.setUser1UnreadCount(room.getUser1UnreadCount() + 1);
        } else {
            room.setUser2UnreadCount(room.getUser2UnreadCount() + 1);
        }

        chatRoomRepo.save(room); // 채팅방 정보 갱신

        return saved;
    }


    @Override
    public List<ChatMessage> getMessages(Long roomId) {
        return chatMessageRepo.findByRoomIdOrderByCreatedAtAsc(roomId);
    }

    @Override
    @Transactional
    public void markAsRead(Long roomId, Integer receiverId) {
        List<ChatMessage> unread = chatMessageRepo.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .filter(m -> m.getReceiverId().equals(receiverId) && !m.getIsRead())
                .toList();

        unread.forEach(m -> {
            m.setIsRead(true);
            m.setReadAt(LocalDateTime.now());
        });
        chatMessageRepo.saveAll(unread);

        // 안읽은 개수 0으로
        chatRoomRepo.findById(roomId).ifPresent(room -> {
            if (room.getUser1Id().equals(receiverId)) {
                room.setUser1UnreadCount(0);
            } else {
                room.setUser2UnreadCount(0);
            }
            chatRoomRepo.save(room);
        });
    }

    @Override
    @Transactional
    public void exitRoom(Long roomId, Integer userId) {
        ChatRoom room = chatRoomRepo.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));

        if (Objects.equals(room.getUser1Id(), userId)) {
            room.setUser1Exit(true);
        } else if (Objects.equals(room.getUser2Id(), userId)) {
            room.setUser2Exit(true);
        }

        // 양쪽 다 나간 경우 → 채팅방과 메시지 삭제
        if (Boolean.TRUE.equals(room.getUser1Exit()) && Boolean.TRUE.equals(room.getUser2Exit())) {
            chatMessageRepo.deleteAll(chatMessageRepo.findByRoomIdOrderByCreatedAtAsc(roomId));
            chatRoomRepo.delete(room);
        } else {
            chatRoomRepo.save(room); // 상태만 반영
        }
    }

    @Override
    public List<ChatRoom> getRoomsByUser(Integer userId) {
        return chatRoomRepo.findByUser1IdOrUser2Id(userId, userId);
    }
}

