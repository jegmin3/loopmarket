package com.loopmarket.common.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

//@Service
public class RedisTestService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisTestService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void testRedisConnection() {
        try {
            redisTemplate.opsForValue().set("testKey", "testValue");
            String value = redisTemplate.opsForValue().get("testKey");
            if ("testValue".equals(value)) {
                System.out.println("✅ Redis Template을 통한 연결 및 데이터 저장/조회 성공!");
            } else {
                System.err.println("❌ Redis Template 연결 성공, 그러나 데이터 문제 발생.");
            }
        } catch (Exception e) {
            System.err.println("❌ Redis Template 연결 시도 중 예외 발생: " + e.getMessage());
            // 이 예외가 발생하면 RedisConnectionFactory 자체가 실패한 것.
        }
    }
}
