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
                        .requestMatchers("/favicon.ico","/image/**", "/auth/**", "/public/**", "/join", "/login", "/", "/signup", "/index.html", "/login.html", "/signup.html").permitAll()

                        // 인증이 필요한 요청
                        .requestMatchers("/mypage.html", "/chid.html")  // 해당 페이지는
                        .hasAnyRole("회원", "관리자")                   // 회원(010), 관리자(020) 만접속 가능

                        .anyRequest().authenticated()

                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
        // 로그아웃 설정
        http.logout((logout) -> logout
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
        http.addFilterBefore(new JWTFilter(jwtUtil, commonCodeRepository), UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 등록
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), memberService, jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
