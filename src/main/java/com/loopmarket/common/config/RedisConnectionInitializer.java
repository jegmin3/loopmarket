package com.loopmarket.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Component; // Component 어노테이션 추가

import com.loopmarket.common.service.RedisTestService;


@Component // Spring 빈으로 등록하여 ApplicationRunner가 동작하도록 합니다.
public class RedisConnectionInitializer implements ApplicationRunner {

    private final ApplicationContext applicationContext; // 현재 애플리케이션 컨텍스트 주입
    
    // 생성자 주입 (권장 방식)
    public RedisConnectionInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("--- 애플리케이션 시작 후 Redis 연결 상태 확인 ---");

        try {
            // 현재 활성화된 SessionRepository 빈을 가져옵니다.
            // @Primary 어노테이션 덕분에 이전에 설정한 빈 중 우선순위가 높은 것이 가져와집니다.
            SessionRepository<?> sessionRepository = applicationContext.getBean(SessionRepository.class);

            if (sessionRepository instanceof RedisIndexedSessionRepository) {
                System.out.println("✅ 현재 RedisSessionRepository (Redis 세션)가 활성화되어 있습니다.");
                // Redis KeySpace Notifications 상태 확인 또는 필요시 추가 설정 로직 (예시)
                // RedisIndexedSessionRepository redisRepo = (RedisIndexedSessionRepository) sessionRepository;
                // redisRepo.enableKeySpaceNotifications(true); // 이미 Spring Boot가 자동으로 처리하지만, 명시적 확인
            } else if (sessionRepository instanceof MapSessionRepository) {
                System.out.println("✅ 현재 MapSessionRepository (JVM 메모리 세션)가 활성화되어 있습니다.");
            } else {
                System.out.println("❓ 알 수 없는 SessionRepository 구현체가 활성화되어 있습니다: " + sessionRepository.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("❌ SessionRepository 빈을 가져오는 중 오류 발생: " + e.getMessage());
            // 이 예외는 이론적으로는 발생하지 않아야 합니다. (항상 둘 중 하나는 존재하므로)
        }
        System.out.println("--- Redis 연결 상태 확인 완료 ---");
    }
}
