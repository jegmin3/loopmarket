package com.loopmarket.domain.admin.notice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {
    private Integer noticeId;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private boolean published;
}