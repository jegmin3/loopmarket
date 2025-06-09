package com.loopmarket.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
	
    /**
     * FirebaseApp Bean 등록
     * Firebase 서비스 계정 키 파일을 읽어 초기화함.
     */
    @Bean
    FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
        	// 서비스 계정 키 파일 경로(resource기준이 아니라 classpath기준 경로), 키 불러옴
        	// classpath 기준 경로로부터 InputStream 얻기
            ClassPathResource resource = new ClassPathResource("config/firebase-service-key.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        //FirebaseApp app = FirebaseApp.getInstance();
        //System.out.println("FirebaseApp name: " + app.getName());
        //System.out.println("FirebaseApp options: " + app.getOptions());

        return FirebaseApp.getInstance(); // 이미 초기화된 경우 그대로 반환
    }

    /**
     * FirebaseMessaging Bean 등록
     * FirebaseApp을 기반으로 메시징 기능 사용
     */
    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
