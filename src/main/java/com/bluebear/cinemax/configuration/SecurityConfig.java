package com.bluebear.cinemax.configuration; // Hoặc package phù hợp với cấu trúc dự án của bạn

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Vô hiệu hóa CSRF cho các đường dẫn không yêu cầu session (webhook, api)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/webhook/**", "/cashier/**") // Thêm /cashier/** vào đây
                )
                // Cấu hình phân quyền
                .authorizeHttpRequests(auth -> auth
                        // Dòng quan trọng: Cho phép truy cập không cần đăng nhập vào webhook VÀ cashier
                        .requestMatchers("/webhook/**", "/cashier/**").permitAll()
                        // Bất kỳ request nào khác đều cần đăng nhập (nếu có trang admin chẳng hạn)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}