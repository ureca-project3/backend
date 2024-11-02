package com.triple.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.auth.handler.OAuthLoginSuccessHandler;
import com.triple.backend.auth.handler.OAuthLoginFailureHandler;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.common.repository.CommonCodeRepository;
import com.triple.backend.member.entity.Member;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import com.triple.backend.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final MemberService memberService;
    private final JWTFilter jwtFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // HTTP 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // LoginFilter 인스턴스 생성
        LoginFilter loginFilter = new LoginFilter(
                authenticationManager(authenticationConfiguration),
                memberService,
                jwtUtil
        );

        // 로그인 URL 설정
        loginFilter.setFilterProcessesUrl("/login");
        // 기본 파라미터 이름 설정
        loginFilter.setUsernameParameter("email");
        loginFilter.setPasswordParameter("password");

        // 인증 실패 핸들러 설정
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            log.error("Authentication failed: {}", exception.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Authentication failed: " + exception.getMessage());
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        });

        httpSecurity
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/books/**",    // 모든 books 관련 엔드포인트 허용
                                "/event/api/**",
                                "/favicon.ico",
                                "/public/**",
                                "/login",
                                "/*.html",      // 모든 html 파일 허용
                                "/css/**",
                                "/js/**",
                                "/image/**"
                        ).permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login.html")
                        .successHandler(oAuthLoginSuccessHandler)
                        .failureHandler(oAuthLoginFailureHandler)
                        .authorizationEndpoint(authorization ->
                                authorization.baseUri("/oauth2/authorization"))
                );

        // 필터 순서
        httpSecurity.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterAfter(jwtFilter, LoginFilter.class);

        return httpSecurity.build();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Child-ID"));
        configuration.setExposedHeaders(Arrays.asList("X-Child-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}