package com.loopmarket.domain.mypage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.images.path}")
    private String uploadPath;  // "upload/profile"

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 절대 경로 (file:/... 형태) 변환
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/profile-images/**")
                .addResourceLocations(absolutePath);
    }
}