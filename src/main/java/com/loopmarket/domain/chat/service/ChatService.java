package com.loopmarket.domain.chat.service;

import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatRoomDTO;
import com.loopmarket.domain.chat.entity.ChatEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.repository.ChatRepository;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository; // MemberRepository 필요 (사용자 정보 조회용)
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations; // 웹소켓 메시지 전송
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 트랜잭션 관리
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 1:1 채팅 관련 비즈니스 로직을 처리하는 서비스 */
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository; // MemberEntity 조회용
    private final SimpMessageSendingOperations messagingTemplate; // 웹소켓 메시지 브로커로 메시지 전송
    //private final FCMService fcmService; // FCMService 주입 (아직 정의되지 않았지만 곧 추가될 예정)

    /**
     * 새로운 채팅 메시지를 처리하고 저장합니다.
     * 채팅방이 없으면 생성하고, 마지막 메시지 정보를 업데이트합니다.
     *
     * @param chatMessageDTO 클라이언트로부터 수신한 메시지 DTO
     * @return 저장된 ChatMessageDTO (필요에 따라)
     */
    @Transactional
    public ChatMessageDTO handleChatMessage(ChatMessageDTO chatMessageDTO) {
        // 1. DTO에서 필요한 엔티티 조회
        MemberEntity sender = memberRepository.findById(chatMessageDTO.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid senderId: " + chatMessageDTO.getSenderId()));
        MemberEntity receiver = memberRepository.findById(chatMessageDTO.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid receiverId: " + chatMessageDTO.getReceiverId()));

        // 2. 채팅방 ID 생성 (두 사용자 ID를 기준으로 정렬)
        String roomId = ChatRoomEntity.generateRoomId(sender.getUserId(), receiver.getUserId());
        chatMessageDTO.setRoomId(roomId); // DTO에도 roomId 설정

        // 3. 채팅방 존재 여부 확인 및 생성/업데이트
        ChatRoomEntity chatRoom = findOrCreateChatRoom(sender, receiver, roomId);

        // 4. ChatMessageDTO -> ChatEntity 변환 및 저장
        ChatEntity chatMessage = ChatEntity.builder()
                .roomId(roomId)
                .sender(sender)
                .receiver(receiver)
                .message(chatMessageDTO.getMessage())
                .type(chatMessageDTO.getType())
                .readStatus(false) // 초기 메시지는 읽지 않은 상태
                .build();
        chatRepository.save(chatMessage);

        // 5. ChatRoomEntity의 마지막 메시지 정보 업데이트
        chatRoom.setLastMessagePreview(chatMessage.getMessage());
        chatRoom.setLastMessageTime(chatMessage.getSentAt());
        chatRoomRepository.save(chatRoom); // 변경된 chatRoom 저장

        // 6. 웹소켓을 통해 해당 채팅방 구독자들에게 메시지 전송
        // "/sub/chat/room/{roomId}" 로 메시지를 보냅니다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, chatMessageDTO);

        // 7. 상대방이 현재 온라인 상태가 아니라면 FCM 알림 발송 (추가 예정인 FCMService 활용)
        // TODO: 실제 사용자 온라인 상태 체크 로직 필요 (예: 세션 관리, Redis 활용)
        boolean isReceiverOnline = false; // 임시로 오프라인으로 가정하여 알림 발송
        if (!isReceiverOnline && ChatEntity.MessageType.TALK.equals(chatMessageDTO.getType())) {
            // fcmService.sendNotification(receiver.getUserId(), sender.getNickname(), chatMessageDTO.getMessage());
            // FCMService 구현 후 주석 해제
        }

        return ChatMessageDTO.from(chatMessage); // 저장된 엔티티 기반으로 DTO 반환
    }

    /**
     * 두 사용자 간의 채팅방을 찾거나 새로 생성합니다.
     * @param user1 첫 번째 사용자 엔티티
     * @param user2 두 번째 사용자 엔티티
     * @param roomId 생성된 채팅방 ID
     * @return ChatRoomEntity
     */
    @Transactional
    public ChatRoomEntity findOrCreateChatRoom(MemberEntity user1, MemberEntity user2, String roomId) {
        return chatRoomRepository.findByRoomId(roomId)
                .orElseGet(() -> {
                    // 채팅방이 없으면 새로 생성
                    ChatRoomEntity newRoom = ChatRoomEntity.builder()
                            .roomId(roomId)
                            .user1(user1)
                            .user2(user2)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
    }


    /**
     * 특정 채팅방의 메시지 기록을 조회합니다.
     * @param roomId 조회할 채팅방 ID
     * @return 메시지 DTO 목록
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public List<ChatMessageDTO> getChatMessages(String roomId) {
        List<ChatEntity> messages = chatRepository.findByRoomIdOrderBySentAtAsc(roomId);
        return messages.stream()
                .map(ChatMessageDTO::from) // ChatEntity -> ChatMessageDTO 변환
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 모든 채팅방 목록을 조회합니다.
     * 각 채팅방의 마지막 메시지 미리보기와 읽지 않은 메시지 수를 포함합니다.
     *
     * @param userId 채팅방 목록을 조회할 사용자 ID
     * @return 채팅방 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomsForUser(Integer userId) {
        List<ChatRoomEntity> chatRooms = chatRoomRepository.findByUser1_UserIdOrUser2_UserIdOrderByLastMessageTimeDesc(userId, userId);

        return chatRooms.stream().map(room -> {
            ChatRoomDTO dto = ChatRoomDTO.from(room, userId); // DTO 변환 (상대방 정보 포함)
            // 읽지 않은 메시지 수 계산 및 설정
            long unreadCount = chatRepository.countByRoomIdAndReceiver_UserIdAndReadStatusFalse(room.getRoomId(), userId);
            dto.setUnreadMessageCount((int) unreadCount);
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 모든 메시지를 '읽음' 상태로 업데이트합니다.
     * @param roomId 업데이트할 채팅방 ID
     * @param userId 메시지를 읽는 사용자 ID
     */
    @Transactional
    public void markMessagesAsRead(String roomId, Integer userId) {
        List<ChatEntity> unreadMessages = chatRepository.findByRoomIdAndReceiver_UserIdAndReadStatusFalse(roomId, userId);
        unreadMessages.forEach(message -> message.setReadStatus(true));
        chatRepository.saveAll(unreadMessages); // 변경된 메시지들을 한 번에 저장
    }

    // TODO: FCMService는 별도 파일로 정의해야 합니다. (FCMService 클래스 사용)
    // 현재는 FCMService가 이 파일에 없으므로 주석 처리하거나, 별도로 FCMService.java 파일을 생성해야 합니다.
    /**
     * 특정 채팅방과 관련된 모든 메시지 및 채팅방 자체를 삭제합니다.
     *
     * @param roomId 삭제할 채팅방 ID
     */
    @Transactional
    public void deleteChatRoomAndMessages(String roomId) {
        // 1. 해당 채팅방의 모든 메시지 삭제
        // JPQL 쿼리나 deleteAll(Iterable<S>) 등을 사용할 수 있습니다.
        // 여기서는 간단하게 먼저 조회 후 삭제합니다.
        List<ChatEntity> messages = chatRepository.findByRoomIdOrderBySentAtAsc(roomId);
        if (!messages.isEmpty()) {
            chatRepository.deleteAll(messages);
        }
        System.out.println("Deleted messages for room: " + roomId);

        // 2. 채팅방 엔티티 삭제
        Optional<ChatRoomEntity> chatRoomOptional = chatRoomRepository.findByRoomId(roomId);
        if (chatRoomOptional.isPresent()) {
            chatRoomRepository.delete(chatRoomOptional.get());
            System.out.println("Deleted chat room: " + roomId);
        } else {
            throw new IllegalArgumentException("Chat room not found with ID: " + roomId);
        }
    }

    // TODO: (선택 사항) 사용자 온라인 상태를 체크하는 실제 로직 구현 (FCM 발송 조건으로 사용)
    // 이 메서드는 예시이며, 실제로는 UserSessionService 같은 것을 만들어서 관리해야 함.
    private boolean isUserOnline(Integer userId) {
        // 실제 구현: 웹소켓 세션 정보를 관리하는 Map이나 Redis 등을 활용
        // 예: return onlineUserSessions.containsKey(userId);
        return false; // 현재는 항상 오프라인으로 간주하여 FCM 발송
    }
    
}
