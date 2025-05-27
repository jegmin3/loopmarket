package com.loopmarket.common.controller;

import com.loopmarket.common.service.GptService;
import com.loopmarket.common.dto.ProductAiRequest;
import com.loopmarket.common.dto.ProductAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class GptApiController {

  private final GptService gptService;

  @PostMapping("/ai-generate")
  public ProductAiResponse generateByAi(@RequestBody ProductAiRequest request) {
    return gptService.generateFromImage(request);
  }
}
