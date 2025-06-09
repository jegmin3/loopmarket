package com.loopmarket.domain.admin.alram;

import lombok.Data;

@Data
public class AlramRequestDTO {
    private String title;
    private String content;
    //private String url;
    private Integer userId; // null이면 전체 대상
}

