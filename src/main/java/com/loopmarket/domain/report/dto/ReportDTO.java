package com.loopmarket.domain.report.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
	private Long reportId;        // 추가
    private Integer reporterId;
    
    // product테이블내용 추가
    private String productTitle;  // 새로 추가
    
    // users테이블 내용추가
    private String nickname;	// 새로추가
    
    private Integer reportedUserId;
    private Integer productId;
    private String reason;
    private String detail;
    private LocalDateTime createdAt;  // 추가
    private boolean processed; // 추가
    
    // 관리자 답변 추가
    private String responseMessage;
    private LocalDateTime respondedAt;
}