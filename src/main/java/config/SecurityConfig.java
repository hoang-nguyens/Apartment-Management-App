package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Tắt CSRF nếu không dùng POST với form truyền thống
                .authorizeHttpRequests()
                .anyRequest().permitAll() // Cho phép tất cả request không cần login
                .and()
                .formLogin().disable() // Không sử dụng form login
                .httpBasic().disable(); // Không sử dụng Basic Auth
        return http.build();
    }
}
