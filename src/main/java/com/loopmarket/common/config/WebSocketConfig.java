package com.loopmarket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.loopmarket.domain.chat.ChatHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatHandler chatHandler;
    
    // RequiredArgsContructor 안쓰고 그냥 생성자 만듦
    public WebSocketConfig(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    
    /**
     * 웹소켓 연결을 위한 설정
     * 웹소켓 연결 EndPoint: ws://localhost:8080/chats
     * 에 연결시 동작할 핸들러는 webSocketChatHandler
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/socket/chat/{roomId}")
                .setAllowedOrigins("*"); // 개발 중에만 모두 허용, 배포 시 도메인 제한예정
    }
}

