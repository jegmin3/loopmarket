package com.loopmarket.domain.alram.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlramDTO {

    private Integer alramId;
    private String content;
    private Boolean read;
    private LocalDateTime createdAt;
}

