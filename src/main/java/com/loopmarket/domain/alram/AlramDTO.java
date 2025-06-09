package com.loopmarket.domain.alram;

import lombok.*;
import java.time.LocalDateTime;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlramDTO {

    private Long alramId;
    private Integer userId;
    private Integer senderId;
    private String type;
    private String title;
    private String content;
    private String url;
    private Boolean isRead;
    // string 포맷으로 바꿀지 말지 고민중
    private LocalDateTime createdAt;
    
    private String receiverName; // 수신자 닉네임 or 이메일
    /** 관리자의 알림 조회용 정적 메서드 */
    public static AlramDTO fromEntity(AlramEntity entity, MemberRepository memberRepository) {
        MemberEntity receiver = memberRepository.findById(entity.getUserId()).orElse(null);
        String receiverName = receiver != null ? receiver.getNickname() : "알 수 없음";
        
        return AlramDTO.builder()
                .alramId(entity.getAlramId())
                .userId(entity.getUserId())
                .senderId(entity.getSenderId())
                .type(entity.getType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .url(entity.getUrl()) // 관리자 알림이면 null 가능
                .isRead(entity.getIsRead())
                .createdAt(entity.getCreatedAt())
                .receiverName(receiverName) //수신자 이름
                .build();
    }

}

