package com.loopmarket.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
public class ProductAiResponse {

  private String title;
  private String description;
  private int ctgCode;

}
