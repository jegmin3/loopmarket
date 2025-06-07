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
        try {
            System.out.println("알림 DTO 확인: " + dto);

            AlramEntity entity = AlramEntity.builder()
                    .userId(dto.getUserId())
                    .senderId(dto.getSenderId())
                    .type(dto.getType())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .url(dto.getUrl())
                    .isRead(false)
                    .build();

            alramRepository.save(entity);
            System.out.println("알림 저장 성공: " + entity);

            // FCM 전송
            MemberEntity receiver = memberRepository.findById(dto.getUserId()).orElse(null);
            if (receiver != null && receiver.getFcmToken() != null) {
            	System.out.println("createAlram에서 FCM전송 조건 만족됨!");
            	System.out.println("알림 제목: " + dto.getTitle());
            	System.out.println("알림 내용: " + dto.getContent());
            	Map<String, String> data = new HashMap<>();
            	data.put("title", dto.getTitle());
            	data.put("body", dto.getContent());

            	Message message = Message.builder()
            	    .setToken(receiver.getFcmToken())
            	    .putAllData(data) // Notification 말고 data-only 메시지로 보냄
            	    .build();
            	System.out.println("📦 FCM 보낼 토큰: " + receiver.getFcmToken());
            	System.out.println("📦 FCM 보낼 data: " + data);
                try {
                    firebaseMessaging.send(message);
                    System.out.println("FCM 전송 성공!");
                } catch (FirebaseMessagingException e) {
                	// SenderId mismatch 뭐지
                	// 1. 익명 로그인이라 2. localhost에서 테스트라 3. fcm설정 안맞는데 fallback중임
                    log.warn("FCM 전송 실패: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("알림 저장 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
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

