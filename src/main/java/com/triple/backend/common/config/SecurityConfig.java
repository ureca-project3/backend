package com.triple.backend.common.config;

import com.triple.backend.common.repository.CommonCodeRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.triple.backend.member.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CommonCodeRepository commonCodeRepository;
    private final MemberService memberService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**", "/join", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // JWT 필터 등록
        http.addFilterBefore(new JWTFilter(jwtUtil, commonCodeRepository), UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 등록
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), memberService, jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 로그아웃 설정
        http.logout((logout) -> logout
                .logoutUrl("/logout") // 로그아웃 요청 URL
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 로그아웃 성공 시의 동작 설정
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"로그아웃 성공\"}");

                    // 추가로 클라이언트에게 액세스 토큰 및 리프레시 토큰 삭제 안내
                    response.getWriter().write("{\"message\": \"로그아웃 성공\", \"info\": \"클라이언트에서 액세스 토큰 및 리프레시 토큰을 삭제하세요.\"}");
                })
                .deleteCookies("Refresh-Token")); // 리프레시 토큰 쿠키 삭제

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
