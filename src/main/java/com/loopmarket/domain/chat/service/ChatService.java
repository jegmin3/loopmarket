package com.loopmarket.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loopmarket.domain.chat.dto.ChatRoomSummaryDTO;
import com.loopmarket.domain.chat.dto.UnreadChatDTO;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.repository.ChatMessageRepository;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.product.service.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ProductService productService;
    //private final ImageRepository imageRepository;
    
    /**
     * 두 유저 간의 채팅방을 찾거나, 없으면 새로 생성해서 반환
     */
    @Transactional
    public ChatRoomEntity enterRoom(Integer myId, Integer targetId, Integer productId) {
        // 항상 user1 < user2로 정렬 (중복 방지)
        Integer user1 = Math.min(myId, targetId);
        Integer user2 = Math.max(myId, targetId);
        // 기존 채팅방 존재 확인
        return chatRoomRepository
                .findByUser1IdAndUser2IdAndProductId(user1, user2, productId)
                .orElseGet(() -> {
                    ChatRoomEntity newRoom = new ChatRoomEntity();
                    newRoom.setUser1Id(user1);
                    newRoom.setUser2Id(user2);
                    newRoom.setProductId(productId);
                    return chatRoomRepository.save(newRoom);
                });
    }
    
    /** 채팅방이 이미 있는지 확인만 하고, 없으면 생성하지 않음 */
    public ChatRoomEntity findExistingRoom(Integer user1, Integer user2, Integer productId) {
    	// 항상 user1 < user2로 정렬 (중복 방지)
        Integer u1 = Math.min(user1, user2);
        Integer u2 = Math.max(user1, user2);

        return chatRoomRepository
            .findByUser1IdAndUser2IdAndProductId(u1, u2, productId)
            .orElse(null);
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
        if (roomId == null) {
            throw new IllegalArgumentException("roomId는 null일 수 없습니다.");
        }
    	
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
            // 상대방이 보낸 메시지만 읽음처리
            if (!msg.getSenderId().equals(userId)) {
                msg.setRead(true);
            }
        }
        // 영속성 컨텍스트 반영 후 재조회
        chatMessageRepository.flush();
        
        return chatMessageRepository.findByRoomIdOrderBySentAtAsc(roomId);
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

        // 메시지가 0개인 방은 제외
        return allRooms.stream()
                .filter(room ->
                    (room.getUser1Id().equals(userId) && !room.isUser1Leaved()) ||
                    (room.getUser2Id().equals(userId) && !room.isUser2Leaved())
                )
                .collect(Collectors.toList()); // toList는 java15이하에서 호환 안됨.
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
            
            MemberEntity opponent = memberRepository.findById(opponentId)
                    .orElseThrow(() -> new IllegalArgumentException("상대방 정보를 찾을 수 없습니다."));
            String profileImagePath = productService.getProfileImagePath(opponent.getProfileImgId()); //프로필 이미지

            // 마지막 메시지 조회
            ChatMessageEntity lastMessage = chatMessageRepository.findTopByRoomIdOrderBySentAtDesc(room.getRoomId())
                    .orElse(null);
            
            // 안 읽은 메시지 수 조회 (상대방이 보낸 메시지이면서, 내가 안 읽은 메시지)
            int unreadCount = chatMessageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(
                    room.getRoomId(), userId);
            
            // 마지막 메시지를 내가 보낸 경우인지 확인
            boolean lastMine = lastMessage != null && lastMessage.getSenderId().equals(userId);
            
            return new ChatRoomSummaryDTO(
                room,
                nickname,
                lastMessage != null ? lastMessage.getContent() : "(메시지 없음)",
                lastMessage != null ? lastMessage.getSentAt() : null,
                unreadCount,
                lastMine,
                profileImagePath
            );
        }).collect(Collectors.toList());
    }
    
    /** 채팅방별 안읽은 메시지 갱신용 */
    public List<UnreadChatDTO> getUnreadSummariesWithLastMessage(Integer userId) {
        List<ChatRoomEntity> rooms = getActiveChatRooms(userId);

        return rooms.stream().map(room -> {
            // 안읽은 개수
            int unreadCount = chatMessageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getRoomId(), userId);

            // 마지막 메시지
            ChatMessageEntity lastMessage = chatMessageRepository.findTopByRoomIdOrderBySentAtDesc(room.getRoomId())
                    .orElse(null);

            String content = (lastMessage != null) ? lastMessage.getContent() : "(메시지 없음)";
            LocalDateTime time = (lastMessage != null) ? lastMessage.getSentAt() : null;

            return new UnreadChatDTO(room.getRoomId(), unreadCount, content, time);
        }).collect(Collectors.toList());
    }
    
    // 상품 이미지(썸네일) 경로 추출
    public String getThumbnailPathForProduct(Integer productId) {
        return "";
    }


    
    
}

