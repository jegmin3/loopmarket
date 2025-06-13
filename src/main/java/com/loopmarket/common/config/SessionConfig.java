package com.loopmarket.common.config;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

/**
 * Spring Boot 애플리케이션의 세션 저장소 설정을 담당하는 Configuration 클래스입니다.
 * Redis 서버의 가용성(실행 여부)에 따라 세션 저장소를 동적으로 변경합니다.
 * - Redis가 실행 중이면 Redis를 세션 저장소로 사용합니다.
 * - Redis가 꺼져 있으면 JVM(메모리)을 세션 저장소로 사용합니다.
 */
@Configuration
public class SessionConfig {

    /**
     * RedisConnectionFactory 빈이 성공적으로 생성되었을 때 (즉, Redis 서버가 실행 중일 때)
     * Redis 기반의 세션 저장소인 RedisIndexedSessionRepository 빈을 생성합니다.
     *
     * `@ConditionalOnBean(RedisConnectionFactory.class)`:
     * - Spring Boot의 자동 설정이 application.yml에 명시된 Redis 연결 정보(host, port)를 사용하여
     * RedisConnectionFactory 빈을 생성하려고 시도합니다.
     * - Redis 서버에 성공적으로 연결되면 이 RedisConnectionFactory 빈이 생성되고,
     * 이 조건이 만족되어 이 메서드(redisSessionRepository)가 실행됩니다.
     *
     * `@Primary`:
     * - 동일한 타입(SessionRepository)의 빈이 여러 개 있을 경우, 이 빈을 우선적으로 사용하도록 지정합니다.
     * (Redis 세션과 JVM 세션 중 하나만 활성화되지만, 명시적으로 우선순위를 부여합니다.)
     *
     * @param redisTemplate Redis 작업을 위한 RedisTemplate 빈을 주입받습니다.
     * Spring Boot는 spring-boot-starter-data-redis 의존성만 있으면
     * 기본 RedisTemplate 빈을 자동으로 생성합니다.
     * @return RedisIndexedSessionRepository 인스턴스 (Redis 세션 저장소)
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    @Primary
    SessionRepository<?> redisSessionRepository( // 반환 타입을 SessionRepository<?>로 변경
            RedisTemplate<Object, Object> redisTemplate) {
        System.out.println("--- RedisIndexedSessionRepository가 활성화되었습니다. (Redis 사용 중) ---");
        // RedisIndexedSessionRepository의 생성자는 RedisOperations (RedisTemplate의 상위 인터페이스)를 인자로 받습니다.
        return new RedisIndexedSessionRepository(redisTemplate);
    }

    /**
     * RedisConnectionFactory 빈이 컨텍스트에 존재하지 않을 때 (즉, Redis 서버가 꺼져 있거나 연결 실패 시)
     * JVM 메모리 기반의 세션 저장소인 MapSessionRepository 빈을 생성합니다.
     *
     * `@ConditionalOnMissingBean(RedisConnectionFactory.class)`:
     * - 위에 정의된 RedisConnectionFactory 빈이 생성되지 않았을 때만 이 조건이 만족되어
     * 이 메서드(inMemorySessionRepository)가 실행됩니다.
     * - 이는 Redis 연결 실패 시 자동으로 JVM 메모리 세션으로 폴백(fallback)하는 역할을 합니다.
     *
     * `@Primary`:
     * - 동일한 타입(SessionRepository)의 빈이 여러 개 있을 경우, 이 빈을 우선적으로 사용하도록 지정합니다.
     *
     * @return MapSessionRepository 인스턴스 (JVM 메모리 세션 저장소)
     */
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    @Primary
    SessionRepository<MapSession> inMemorySessionRepository() { // MapSession 타입은 그대로 유지
        System.out.println("--- MapSessionRepository (JVM 메모리)가 활성화되었습니다. (Redis 미사용, Fallback) ---");
        // MapSessionRepository는 세션을 저장할 Map 구현체를 인자로 받습니다.
        // ConcurrentHashMap은 멀티스레드 환경에서 안전하게 사용할 수 있는 Map입니다.
        return new MapSessionRepository(new ConcurrentHashMap<String, Session>());
    }
}
