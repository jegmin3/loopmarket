package com.loopmarket.domain.chat.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener{
	
	@Autowired
	private ChatSessionTracker sessionTracker;
	
	@EventListener
	public void handleSessionConnect(SessionConnectEvent event) {
	    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
	    String sessionId = accessor.getSessionId();

	    // 커스텀 헤더에서 userId, roomId 꺼내기 (클라이언트에서 전송해야 함)
	    String userIdStr = accessor.getFirstNativeHeader("userId");
	    String roomIdStr = accessor.getFirstNativeHeader("roomId");
	    
	    //System.out.println("소켓 연결 요청됨: sessionId=" + sessionId + ", userId=" + userIdStr + ", roomId=" + roomIdStr);
	    
	    if (userIdStr != null && roomIdStr != null) {
	        Integer userId = Integer.parseInt(userIdStr);
	        Long roomId = Long.parseLong(roomIdStr);
	        
	        sessionTracker.register(sessionId, userId, roomId);
	        sessionTracker.userEnterRoom(roomId, userId);
	    }
	}
	
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        // 세션 ID를 userId, roomId로 매핑해둬야 함
        String sessionId = event.getSessionId();
        //System.out.println("웹소켓 세션 종료됨: " + sessionId);
        
        Integer userId = sessionTracker.getUserId(sessionId);
        Long roomId = sessionTracker.getRoomId(sessionId);

        if (userId != null && roomId != null) {
        	sessionTracker.userLeaveRoom(roomId, userId);
        	sessionTracker.unregister(sessionId);
            System.out.println("✅ 사용자 방 나감 처리됨: userId=" + userId + ", roomId=" + roomId);
        }
    }
}

