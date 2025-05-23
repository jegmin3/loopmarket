package com.loopmarket.domain.alram.service;

import java.util.List;

import com.loopmarket.domain.alram.AlramEntity;

public interface AlramService {
	// 안읽은 알림
    List<AlramEntity> getUnreadAlrams(Integer userId);
    // 알림 저장
    void saveAlram(Integer userId, String content);
    // 알림 읽기
    void markAllAsRead(Integer userId);
}

