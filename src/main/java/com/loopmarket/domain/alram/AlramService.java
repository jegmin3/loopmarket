package com.loopmarket.domain.alram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

// Slf4j = Logger log = loggerFactory.getLogger... 와 같은 역할
@Slf4j
@Service
@RequiredArgsConstructor
public class AlramService {

    private final AlramRepository alramRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;

    /**
     * 알림 생성
     */
    public void createAlram(AlramDTO dto) {
    	// 저장 로직
        AlramEntity entity = AlramEntity.builder()
                .userId(dto.getUserId())
                .senderId(dto.getSenderId())
                .type(dto.getType())
                .title(dto.getTitle())
                .content(dto.getContent())
                .url(dto.getUrl())
                .isRead(false) // 생성 시 기본값 false
                .build();

        alramRepository.save(entity);
        
        // FCM 전송
        MemberEntity receiver = memberRepository.findById(dto.getUserId()).orElse(null);
        if (receiver != null && receiver.getFcmToken() != null) {
            Notification notification = Notification.builder()
                    .setTitle(dto.getTitle())
                    .setBody(dto.getContent())
                    .build();

            Message message = Message.builder()
                    .setToken(receiver.getFcmToken())
                    .setNotification(notification)
                    .build();

            try {
                firebaseMessaging.send(message);
            } catch (FirebaseMessagingException e) {
                log.warn("FCM 전송 실패: {}", e.getMessage());
            }
        }
    }
    
    /**
     * 안 읽은 알림 목록 반환
     */
    public List<AlramDTO> getUnreadAlrams(Integer userId) {
        return alramRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 알림 모두 읽음 처리
     */
    public void markAllAsRead(Integer userId) {
        List<AlramEntity> alrams = alramRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (AlramEntity alram : alrams) {
            alram.setIsRead(true);
        }

        alramRepository.saveAll(alrams);
    }

    /**
     * Entity → DTO 변환
     */
    private AlramDTO toDTO(AlramEntity entity) {
        return AlramDTO.builder()
                .alramId(entity.getAlramId())
                .userId(entity.getUserId())
                .senderId(entity.getSenderId())
                .type(entity.getType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .url(entity.getUrl())
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

