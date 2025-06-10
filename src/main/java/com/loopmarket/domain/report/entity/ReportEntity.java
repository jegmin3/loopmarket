package com.loopmarket.domain.report.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "report")
@Data
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private Integer reporterId;
    private Integer reportedUserId;
    private Integer productId; // nullable 가능

    private String reason;

    @Lob
    private String detail;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean processed = false;
    
    // 관리자 답변 추가
    private String responseMessage;

    private LocalDateTime respondedAt;

}