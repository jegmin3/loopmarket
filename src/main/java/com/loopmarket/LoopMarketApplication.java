package com.loopmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot에서 애플리케이션을 실행하는 시작점이에요. 
 * (C언어나 Java의 main() 함수같은 기능)
 * Configuratio, EnableAutoConfiguration, ComponentScan 3가지 기능을 포함합니다.
 * 구매 10일 후 자동 구매 확정 처리를 위해 @EnableScheduling 추가하였습니다.
 */
@EnableScheduling
@SpringBootApplication
public class LoopMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoopMarketApplication.class, args);
	}

}
