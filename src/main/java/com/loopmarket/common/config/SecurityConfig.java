package com.loopmarket.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

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
	
	// 방화벽 허용 (이미지 불러오기 오류나서 열어뒀습니다 - kw)
    @Bean
    HttpFirewall relaxedFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 이중 슬래시 허용
        firewall.setAllowUrlEncodedDoubleSlash(true);
        firewall.setAllowBackSlash(true); // 백슬래시 헝용
        firewall.setAllowSemicolon(true); // 필요시
        return firewall;
    }
    protected void configure(WebSecurity web) throws Exception {
        web.httpFirewall(relaxedFirewall());
    }

}
