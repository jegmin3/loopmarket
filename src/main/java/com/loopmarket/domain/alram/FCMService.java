package com.loopmarket.domain.alram;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.loopmarket.domain.member.MemberRepository; // FCM 토큰 조회를 위해 MemberRepository 주입
import com.loopmarket.domain.member.MemberEntity; // MemberEntity 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

/** Firebase Cloud Messaging (FCM) 알림 발송 서비스 */
@Service
@RequiredArgsConstructor
public class FCMService {

	//// application.properties에서 설정된 서비스 계정 키 경로 (예: "classpath:config/fcmforchat123612s.json")
    @Value("${firebase.sdk.path}")
    private String firebaseSdkPath;

    private final MemberRepository memberRepository; // 사용자별 FCM 토큰 조회를 위함

    /**
     * 애플리케이션 시작 시 Firebase Admin SDK를 초기화합니다.
     */
    @PostConstruct
    public void initialize() {
        try {
            // 핵심 수정 부분: firebaseSdkPath에서 "classpath:" 접두사를 제거
            // @Value 어노테이션이 이미 classpath:를 포함하고 있으므로,
            // ClassPathResource 생성자에 전달할 때는 순수한 경로만 필요합니다.
            String pathWithoutClasspathPrefix = firebaseSdkPath.startsWith("classpath:") ?
                                                firebaseSdkPath.substring("classpath:".length()) :
                                                firebaseSdkPath;

            ClassPathResource resource = new ClassPathResource(pathWithoutClasspathPrefix); // 수정된 부분

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully.");
            } else {
                System.out.println("Firebase Admin SDK already initialized.");
            }
        } catch (IOException e) {
            System.err.println("Error initializing Firebase Admin SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 특정 사용자에게 FCM 알림을 전송합니다.
     *
     * @param targetUserId 알림을 받을 사용자의 ID
     * @param senderNickname 알림을 보낸 사용자의 닉네임
     * @param messageContent 알림에 포함될 메시지 내용
     */
    public void sendNotification(Integer targetUserId, String senderNickname, String messageContent) {
        // 1. targetUserId의 FCM 토큰을 데이터베이스에서 조회
        Optional<MemberEntity> targetMemberOptional = memberRepository.findById(targetUserId);

        if (targetMemberOptional.isEmpty()) {
            System.out.println("FCM Notification failed: Target user not found with ID " + targetUserId);
            return;
        }

        MemberEntity targetMember = targetMemberOptional.get();
        String fcmToken = targetMember.getFcmToken(); // MemberEntity에 fcmToken 필드가 있다고 가정

        if (fcmToken == null || fcmToken.isEmpty()) {
            System.out.println("FCM Notification skipped: FCM Token not found for user " + targetUserId);
            return;
        }

        // 2. 알림 메시지 생성
        Notification notification = Notification.builder()
                .setTitle(senderNickname + "님으로부터 메시지") // 알림 제목
                .setBody(messageContent) // 알림 본문
                .build();

        // 3. FCM 메시지 빌드
        Message message = Message.builder()
                .setToken(fcmToken) // 특정 기기(FCM 토큰)에 알림 전송
                .setNotification(notification)
                // 추가 데이터 (클라이언트 앱에서 알림 클릭 시 특정 화면으로 이동 등 활용 가능)
                .putData("senderId", String.valueOf(targetMember.getUserId())) // String으로 변환 필요
                .putData("senderNickname", senderNickname)
                .putData("messageContent", messageContent)
                .build();

        // 4. FCM 메시지 전송
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent FCM message to user " + targetUserId + ": " + response);
        } catch (Exception e) {
            System.err.println("Error sending FCM message to user " + targetUserId + ": " + e.getMessage());
            e.printStackTrace();
            // TODO: 재시도 로직 또는 에러 로깅 강화
        }
    }
}
