package com.loopmarket.common.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.loopmarket.domain.member.MemberEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Component
public class HttpHandshakeInterceptor implements HandshakeInterceptor {

    /**
     * WebSocket 연결 시 세션 정보를 가져와 연결을 허용할지 여부를 판단합니다.
     * - 로그인 세션에 사용자 정보가 없으면 연결 차단
     * - 사용자 ID를 WebSocket 세션 속성에 저장하여 컨트롤러에서 사용 가능하게 합니다.
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // HTTP 요청에서 세션 꺼내기
        if (request instanceof ServletServerHttpRequest) {
        	// 자바 11버전이라 intaceof확인후 명시적으로 캐스팅 함 (16이상은 안해도 괜찮)
        	HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            HttpSession session = servletRequest.getSession(); // false → 세션 없으면 null 반환

            if (session != null) {
                MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
                // 로그인 사용자 정보가 있는 경우 WebSocket 허용
                if (loginUser != null) {
                    // WebSocket 세션 속성에 userId 저장 → 이후 컨트롤러에서 꺼내 사용 가능
                    String userId = loginUser.getUserId().toString();
                    System.out.println("WebSocket 세션에 userId 주입: " + userId);
                    attributes.put("userId", userId);  // 여기에 null이 안 들어가야 함
                    return true;
                }
            }
        }

        // 로그인 안 했어도 handshake는 통과 (하지만 서버 처리에선 userId null 체크하면 됨)
        return true;
    }

    /**
     * WebSocket 연결 후 호출되는 후처리 로직 (여기선 별도 처리 없음)
     */
    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}


