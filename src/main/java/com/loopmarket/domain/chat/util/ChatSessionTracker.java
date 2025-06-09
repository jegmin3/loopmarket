package com.loopmarket.domain.chat.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/** 채팅(웹소켓)구독 관리용 트래커 */
@Component
public class ChatSessionTracker {

    // roomId → 접속중인 userId 목록
    private final Map<Long, Set<Integer>> roomSessionMap = new ConcurrentHashMap<>();

    public void userEnterRoom(Long roomId, Integer userId) {
        roomSessionMap.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        //System.out.println(" userEnterRoom 호출됨: " + roomId + ", " + userId);
    }

    public void userLeaveRoom(Long roomId, Integer userId) {
    	System.out.println(" userLeaveRoom 호출됨: " + roomId + ", " + userId);
        Set<Integer> users = roomSessionMap.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                roomSessionMap.remove(roomId);
            }
        }
    }
    
    // 접속시 sessionId기반 맵에 저장
    private final Map<String, Integer> sessionIdToUserId = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionIdToRoomId = new ConcurrentHashMap<>();
    
    public boolean isUserInRoom(Long roomId, Integer userId) {
        return roomSessionMap.getOrDefault(roomId, Set.of()).contains(userId);
    }
    
    public void register(String sessionId, Integer userId, Long roomId) {
        sessionIdToUserId.put(sessionId, userId);
        sessionIdToRoomId.put(sessionId, roomId);
    }

    public void unregister(String sessionId) {
        sessionIdToUserId.remove(sessionId);
        sessionIdToRoomId.remove(sessionId);
    }

    public Integer getUserId(String sessionId) {
        return sessionIdToUserId.get(sessionId);
    }

    public Long getRoomId(String sessionId) {
        return sessionIdToRoomId.get(sessionId);
    }
}
