package com.loopmarket.common.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {

  // 실제 파일이 저장되는 기본 디렉토리 (프로젝트 경로 하위 /upload/images)
  private final String uploadDir = System.getProperty("user.dir") + "/upload/images";

  /**
   * 업로드된 MultipartFile을 서버에 저장하고,
   * DB에 저장할 접근 경로(URL)를 반환한다.
   *
   * @param file 업로드된 파일 객체
   * @return 저장된 파일의 웹 접근 경로 (예: /images/2025-05-22/UUID_파일명)
   */
  public String save(MultipartFile file) {
    try {
      // 1. 오늘 날짜로 폴더 이름 생성 (ex: 2025-05-22)
      String dateDir = LocalDate.now().toString();

      // 2. 저장할 폴더 경로 생성 (upload/images/2025-05-22)
      Path savePath = Paths.get(uploadDir, dateDir);

      // 3. 폴더가 없으면 생성
      Files.createDirectories(savePath);

      // 4. 파일명에 UUID 붙여서 중복 방지
      String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

      // 5. 최종 저장 경로 결정
      Path targetPath = savePath.resolve(fileName);

      // 6. 파일을 실제로 저장
      file.transferTo(targetPath.toFile());

      // 7. DB에 저장할 웹 접근 경로 반환
      return "/images/" + dateDir + "/" + fileName;

    } catch (IOException e) {
      // 저장 중 오류 발생 시 런타임 예외로 던짐
      throw new RuntimeException("파일 저장 실패", e);
    }
  }
}
