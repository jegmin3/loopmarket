package com.loopmarket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** STOMP 기반 웹소켓 메시지 브로커 설정 */
@Configuration
@EnableWebSocketMessageBroker // 웹소켓 메시지 브로커 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커를 구성합니다.
     * 클라이언트가 메시지를 구독하고 발행하는 경로를 정의합니다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 메시지를 구독하는 요청(서버 -> 클라이언트)의 prefix
        // 클라이언트는 이 경로로 구독하여 메시지를 수신합니다. (예: /sub/chat/room/{roomId})
        config.enableSimpleBroker("/sub");

        // 2. 메시지를 발행하는 요청(클라이언트 -> 서버)의 prefix
        // 클라이언트는 이 경로로 메시지를 서버의 @MessageMapping 메서드로 보냅니다. (예: /pub/chat/message)
        config.setApplicationDestinationPrefixes("/pub");
    }

    /**
     * 웹소켓 연결을 위한 STOMP 엔드포인트를 등록합니다.
     * 클라이언트는 이 엔드포인트를 통해 웹소켓 연결을 시도합니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws/chat" 경로로 웹소켓 연결을 허용합니다.
        // SockJS를 사용하여 웹소켓을 지원하지 않는 브라우저에서도 폴백(fallback)을 통해 연결을 유지합니다.
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // 모든 Origin 허용 (개발 환경용, 운영 시에는 특정 도메인으로 제한 권장)
                .withSockJS(); // SockJS 지원 활성화
    }
}

