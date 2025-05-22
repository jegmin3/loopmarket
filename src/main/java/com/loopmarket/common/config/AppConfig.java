package com.loopmarket.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Spring이 PasswordEncoder 타입의 빈을 자동으로 찾아서 
 * @Autowired, @RequiredArgsConstructor, 생성자 등에 주입해줄 수 있도록 해주는 configuration 클래스 */
@Configuration
public class AppConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}