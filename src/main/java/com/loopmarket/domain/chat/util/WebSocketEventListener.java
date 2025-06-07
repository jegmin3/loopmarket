package com.loopmarket.domain.chat.util;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/** 지금은 안쓸거 */
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        // 세션 ID를 userId, roomId로 매핑해둬야 함
        String sessionId = event.getSessionId();
        System.out.println("세션 종료됨: " + sessionId);
        // 여기에 유저 상태 변경이나 리소스 정리 등 할지?
    }
}

