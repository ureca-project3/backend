package com.triple.backend.common.config;

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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration; // AuthenticationConfiguration 의존성 주입
    private final JWTUtil jwtUtil; // jwtUtil 주입
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 생성자
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,  JWTUtil jwtUtil) { // JwtAuthenticationFilter jwtAuthenticationFilter 삭제
        this.authenticationConfiguration = authenticationConfiguration;
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(httpBasic -> httpBasic.disable())    // 노출 방지
                .formLogin(formLogin -> formLogin.disable())    // 기본 로그인 페이지를 비활성화
                .csrf(csrf -> csrf.disable())                   // CSRF 방어 비활성화
                .authorizeHttpRequests(auth -> auth             // 인가 작업
                        .requestMatchers("/auth/**", "/public/**", "/join","/login").permitAll()  // 인증 없이 접근 가능
                        .anyRequest().authenticated()  // 그 외 요청은 인증 필요
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션을 사용하지 않음 (JWT 기반)
                );

        // JWT 필터 추가
//        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        //JWTFilter 등록
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
//        http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        // 로그인 필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}