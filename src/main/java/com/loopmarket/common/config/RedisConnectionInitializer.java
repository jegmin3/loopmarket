package com.loopmarket.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.session.SessionRepository;

//@Component // Spring 빈으로 등록하여 ApplicationRunner가 동작하도록 함.
public class RedisConnectionInitializer implements ApplicationRunner {

    private final ApplicationContext applicationContext; // 현재 애플리케이션 컨텍스트 주입
    
    // 생성자 주입 (권장 방식임)
    public RedisConnectionInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("--- 애플리케이션 시작 후 Redis 연결 상태 확인 ---");
        // getBeanProvider를 사용하면 fallback으로 등록된 자동 구성도 포함됨!
        SessionRepository<?> sessionRepository =
                applicationContext.getBeanProvider(SessionRepository.class).getIfAvailable();

        if (sessionRepository != null) {
            if (sessionRepository.getClass().getName().contains("RedisIndexedSessionRepository")) {
                System.out.println("✅ 현재 RedisSessionRepository (Redis 세션)가 활성화되어 있습니다.");
            } else if (sessionRepository.getClass().getName().contains("MapSessionRepository")) {
                System.out.println("✅ 현재 MapSessionRepository (JVM 메모리 세션)가 활성화되어 있습니다.");
            } else {
                System.out.println("❓ 알 수 없는 SessionRepository 구현체: " + sessionRepository.getClass().getName());
            }
        } else {
            System.out.println("⚠️ SessionRepository 빈이 등록되어 있지 않습니다. 세션 기능이 비활성화될 수 있습니다.");
        }
        
        System.out.println("--- Redis 연결 상태 확인 완료 ---");
    }
}
