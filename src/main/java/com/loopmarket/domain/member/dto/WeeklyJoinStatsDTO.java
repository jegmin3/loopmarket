package com.loopmarket.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyJoinStatsDTO {
    private String week; // 예: 2025-22
    private int count;
}
