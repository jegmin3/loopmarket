package com.loopmarket.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// Spring Security 필터 설정
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	    	// 모든 request 허용
	        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
	        // 원래 post요청시 csrf토큰이 필요하지만 일단 비활성화 해둠
	        .csrf(csrf -> csrf.disable())
	        // security용 로그인폼 비활성화
	        .formLogin(form -> form.disable());
	    return http.build();
	}

}
