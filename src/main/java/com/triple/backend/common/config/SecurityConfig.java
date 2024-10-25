package com.triple.backend.common.config;

import com.triple.backend.auth.handler.OAuthLoginSuccessHandler;
import com.triple.backend.auth.handler.OAuthLoginFailureHandler;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;

    private final AuthenticationConfiguration authenticationConfiguration; // AuthenticationConfiguration 의존성 주입
    private final JWTUtil jwtUtil;
    private final CommonCodeRepository commonCodeRepository;
    private final MemberService memberService;
    private final JWTFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // 클라이언트 도메인
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true); // 쿠키 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }



    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .httpBasic(httpBasic -> httpBasic.disable())    // 노출 방지
                .formLogin(formLogin -> formLogin.disable())    // 기본 로그인 페이지를 비활성화
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                .csrf(csrf -> csrf.disable())                   // CSRF 방어 비활성화
                .authorizeHttpRequests(auth -> auth             // 인가 작업
                        .requestMatchers("/auth/**", "/public/**", "/join","/login").permitAll()  // 인증 없이 접근 가능
                        .anyRequest().authenticated()  // 그 외 요청은 인증 필요
                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // 세션을 사용하지 않음 (JWT 기반)
//                );
                .oauth2Login(oauth -> // OAuth2 로그인 기능에 대한 여러 설정의 진입점
                        oauth
                                .successHandler(oAuthLoginSuccessHandler) // 로그인 성공 시 핸들러
                                .failureHandler(oAuthLoginFailureHandler) // 로그인 실패 시 핸들러
                );
        // 로그아웃 설정
        httpSecurity.logout((logout) -> logout
                .logoutUrl("/logout") // 로그아웃 요청 URL
                .logoutSuccessHandler((request, response, authentication) -> {
                    // 로그아웃 성공 시의 동작 설정
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"로그아웃 성공\"}");


                })
                .deleteCookies("Refresh-Token") // 쿠키 삭제
                .invalidateHttpSession(false)); // 세션 무효화
        // JWT 필터 등록
        httpSecurity.addFilterBefore(new JWTFilter(jwtUtil, commonCodeRepository), UsernamePasswordAuthenticationFilter.class);
        // 로그인 필터 등록
        httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), memberService, jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // JWT 필터 추가
        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}