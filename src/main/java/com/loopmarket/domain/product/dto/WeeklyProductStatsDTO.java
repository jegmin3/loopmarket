package com.loopmarket.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeeklyProductStatsDTO {
    private String week;
    private int count;
}
