package com.triple.backend.common.config;

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
    private final RefreshTokenRepository refreshTokenRepository;

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
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**", "/auth/signupjoin","/auth/login", "/index.html", "/image/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth ->
                        oauth
                                .successHandler(oAuthLoginSuccessHandler)
                                .failureHandler(oAuthLoginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            try {
                                // RefreshToken 쿠키 가져오기
                                Cookie[] cookies = request.getCookies();
                                String refreshToken = null;
                                if (cookies != null) {
                                    for (Cookie cookie : cookies) {
                                        if ("refreshToken".equals(cookie.getName())) {
                                            refreshToken = cookie.getValue();
                                            break;
                                        }
                                    }
                                }

                                // DB에서 RefreshToken 삭제
                                if (authentication != null && authentication.getPrincipal() instanceof Member) {
                                    Member member = (Member) authentication.getPrincipal();
                                    refreshTokenRepository.deleteByMemberId(member.getMemberId());
                                }

                                // RefreshToken 쿠키 삭제
                                Cookie refreshTokenCookie = new Cookie("refreshToken", null);
                                refreshTokenCookie.setMaxAge(0);
                                refreshTokenCookie.setPath("/");
                                refreshTokenCookie.setHttpOnly(true);
                                refreshTokenCookie.setSecure(true);
                                response.addCookie(refreshTokenCookie);

                                // 세션 무효화
                                HttpSession session = request.getSession(false);
                                if (session != null) {
                                    session.invalidate();
                                }

                                // Security Context 정리
                                SecurityContextHolder.clearContext();

                                // JSON 응답 전송
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.setContentType("application/json");
                                response.getWriter().write("{\"message\": \"로그아웃 성공\"}");

                                // index.html로 리다이렉트
                                response.sendRedirect("/index.html?logout");
                            } catch (Exception e) {
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("{\"message\": \"로그아웃 처리 중 오류 발생\"}");
                            }
                        })
                        .invalidateHttpSession(true)
                );

        // JWT 필터 추가
        httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), memberService, jwtUtil), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}