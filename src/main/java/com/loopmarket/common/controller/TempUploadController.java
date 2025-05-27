package com.loopmarket.common.controller;

import com.loopmarket.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TempUploadController {

  private final S3Service s3Service;

  // 이미지 임시 업로드 엔드포인트 (프론트에서 AI 분석용 등으로 호출)
  @PostMapping("/api/upload/temp")
  public ResponseEntity<Map<String, String>> uploadTempImage(@RequestParam("image") MultipartFile image) {
    try {
      String imageUrl = s3Service.uploadFile(image, "temp"); // S3의 temp 폴더에 업로드
      return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "업로드 실패: " + e.getMessage()));
    }
  }
}
