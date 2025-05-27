package com.loopmarket.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductAiRequest {
  private List<String> imageUrls;
}
