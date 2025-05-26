package com.loopmarket.domain.alram.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.loopmarket.domain.alram.AlramEntity;
import com.loopmarket.domain.alram.AlramRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlramServiceImpl implements AlramService {

    private final AlramRepository alramRepository;

    @Override
    public List<AlramEntity> getUnreadAlrams(Integer userId) {
        return alramRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public void saveAlram(Integer userId, String content) {
        AlramEntity alram = AlramEntity.builder()
            .userId(userId)
            .content(content)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();

        alramRepository.save(alram);
    }
    /** 알림 읽기 처리 */
    @Override
    public void markAllAsRead(Integer userId) {
        List<AlramEntity> unreadList = alramRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        for (AlramEntity alram : unreadList) {
            alram.setRead(true);
        }
        alramRepository.saveAll(unreadList);
    }


}

