package com.loopmarket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 서버에 저장된 이미지 파일을 웹에서 접근 가능하도록 설정하는 클래스
 *
 * 예를 들어,
 * 서버 내부의 "/upload/images/2025-05-22/pic.jpg" 파일을
 * 웹에서 "http://도메인/images/2025-05-22/pic.jpg" 경로로 사용할 수 있게 연결한다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * 정적 리소스 핸들러 추가
   * "/images/**" 요청을 실제 파일 시스템 내 "upload/images" 폴더와 매핑한다.
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 서버 내 이미지 저장 폴더 절대 경로 (예: /home/user/project/upload/images/)
    String filePath = System.getProperty("user.dir") + "/upload/images/";

    // "/images/**" 요청을 위 경로와 연결해, 브라우저에서 이미지 파일을 직접 불러올 수 있도록 설정
    registry.addResourceHandler("/images/**")
      .addResourceLocations("file:" + filePath);
  }
}
