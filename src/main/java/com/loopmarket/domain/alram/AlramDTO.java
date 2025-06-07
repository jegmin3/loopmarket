package com.loopmarket.domain.alram;

import lombok.*;
import java.time.LocalDateTime;

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
}

