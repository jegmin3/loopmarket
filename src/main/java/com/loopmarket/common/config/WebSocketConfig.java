package com.loopmarket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * - STOMP 프로토콜 기반 WebSocket 연결
 * - 채팅 메시지 송수신 경로 지정
 */
@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 사용 선언
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 클라이언트가 메시지를 보낼 endpoint 경로 설정
     * ex) /ws/chat으로 WebSocket 연결 요청
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat") // 클라이언트 연결용 endpoint
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS 지원
    }

    /**
     * STOMP 메시지 브로커 구성
     * - /app: 클라이언트가 서버로 메시지 전송할 때 prefix
     * - /queue, /topic: 서버가 클라이언트에게 메시지 보낼 때 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 서버로 보내는 메시지 경로 설정 (컨트롤러 @MessageMapping 핸들링 대상)
        registry.setApplicationDestinationPrefixes("/app");

        // 2. 클라이언트가 구독할 수 있는 경로 설정 (브로커가 처리)
        registry.enableSimpleBroker("/queue", "/topic"); // 내 채널: /queue/myRoom
    }
}

