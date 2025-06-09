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
    }

    public void userLeaveRoom(Long roomId, Integer userId) {
        Set<Integer> users = roomSessionMap.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                roomSessionMap.remove(roomId);
            }
        }
    }

    public boolean isUserInRoom(Long roomId, Integer userId) {
        return roomSessionMap.getOrDefault(roomId, Set.of()).contains(userId);
    }
}
