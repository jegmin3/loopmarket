package com.loopmarket.domain.chat.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/** 지금은 안쓸거 */
@Component
public class WebSocketEventListener {

    @Autowired
    private ChatSessionTracker chatSessionTracker;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        // 세션 ID를 userId, roomId로 매핑해둬야 함
    }
}

