package com.loopmarket.domain.alram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

// Slf4j = Logger log = loggerFactory.getLogger... 와 같은 역할
@Slf4j
@Service
@RequiredArgsConstructor
public class AlramService {

    private final AlramRepository alramRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final MemberRepository memberRepository;
    
    /** 
     * 중복 알림이 있으면 업데이트, 없으면 저장 
     */
    @Transactional
    public void createOrUpdateChatAlram(AlramDTO dto) {
        Optional<AlramEntity> optional = alramRepository.findByUserIdAndUrlAndType(
            dto.getUserId(), dto.getUrl(), dto.getType()
        );

        AlramEntity entity;

        if (optional.isPresent()) {
            // 기존 알림 업데이트
            entity = optional.get();
            entity.setContent(dto.getContent());
            entity.setTitle(dto.getTitle());
            entity.setSenderId(dto.getSenderId());
            entity.setIsRead(false);
            alramRepository.save(entity);
            System.out.println("기존 알림 업데이트 완료!");
        } else {
            // 새 알림 저장
            entity = AlramEntity.builder()
                    .userId(dto.getUserId())
                    .senderId(dto.getSenderId())
                    .type(dto.getType())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .url(dto.getUrl())
                    .isRead(false)
                    .build();
            alramRepository.save(entity);
            System.out.println("새 알림 생성됨");
        }

        // 알림 저장 유무와 관계없이 무조건 FCM 전송
        sendFcm(dto); // 또는 sendFcm(entity);
    }
    
    /**
     * 업데이트든 저장이든 알람은 전송되게 */
    private void sendFcm(AlramDTO dto) {
        MemberEntity receiver = memberRepository.findById(dto.getUserId()).orElse(null);
        if (receiver != null && receiver.getFcmToken() != null) {
            Map<String, String> data = new HashMap<>();
            data.put("title", dto.getTitle());
            data.put("body", dto.getContent());

            Message message = Message.builder()
                .setToken(receiver.getFcmToken())
                .putAllData(data)
                .build();

            try {
                firebaseMessaging.send(message);
                System.out.println("FCM 전송 완료");
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
    
    /**
     * 관리자용 알림 생성(전송)
     */
    public void createAdminAlram(AlramDTO dto) {
        try {
            if (dto.getUserId() == null) {
                log.warn("userId가 null입니다. 알림을 생성하지 않습니다.");
                return;
            }

            AlramEntity entity = AlramEntity.builder()
                    .userId(dto.getUserId())
                    .senderId(dto.getSenderId()) // null 가능
                    .type(dto.getType())         // "ADMIN"
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .isRead(false)
                    .build();

            alramRepository.save(entity); //db저장
            //System.out.println("관리자 알림 저장 완료: " + entity);

            // FCM 전송
            MemberEntity receiver = memberRepository.findById(dto.getUserId()).orElse(null);
            if (receiver != null && receiver.getFcmToken() != null) {
                Map<String, String> data = new HashMap<>();
                data.put("title", dto.getTitle());
                data.put("body", dto.getContent());

                Message message = Message.builder()
                        .setToken(receiver.getFcmToken()) // 개별사용자에게만 전송
                        .putAllData(data)
                        .build();

                try {
                    firebaseMessaging.send(message);
                    System.out.println("관리자 알림 FCM 전송 성공");
                } catch (FirebaseMessagingException e) {
                    log.warn("관리자 알림 FCM 전송 실패: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("관리자 알림 저장 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

