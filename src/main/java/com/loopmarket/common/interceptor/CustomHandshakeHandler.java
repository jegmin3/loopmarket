package com.loopmarket.common.interceptor;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Component
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        // 세션에서 저장해둔 userId 꺼냄
        Object userId = attributes.get("userId");
        
        if (userId != null) {
            System.out.println("Principal 생성됨: " + userId);
            return () -> userId.toString();
        }
        
        // 없으면 null 반환 → 위 에러 발생함
        System.out.println("Principal 생성 실패: userId 없음");
        return null;
    }
}
