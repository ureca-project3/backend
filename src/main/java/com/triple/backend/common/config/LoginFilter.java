package com.triple.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    // 인증 처리 메소드
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    // JWTUtil 주입
    private final JWTUtil jwtUtil;

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String email = request.getParameter("email");
        log.info("Email obtained from request: {}", email);
        return email;
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String password = request.getParameter("password");
        log.info("Password obtained from request: {}", password != null ? "Yes" : "No");
        return password;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            log.info("=== LoginFilter.attemptAuthentication 시작 ===");

            // 요청 파라미터 출력
            log.info("Request parameters:");
            Map<String, String[]> paramMap = request.getParameterMap();
            paramMap.forEach((key, value) -> {
                if (!key.equals("password")) { // 비밀번호는 로그에 출력하지 않음
                    log.info("{}: {}", key, String.join(", ", value));
                }
            });

            String email = obtainUsername(request);
            String password = obtainPassword(request);

            if (email == null || password == null) {
                log.error("이메일 또는 비밀번호가 누락되었습니다.");
                throw new AuthenticationException("Missing credentials") {};
            }

            log.info("사용자 조회 시도: {}", email);
            Member member = memberService.findByEmail(email);

            if (member == null) {
                log.error("해당 이메일의 사용자를 찾을 수 없습니다: {}", email);
                throw new UsernameNotFoundException("User not found");
            }

            log.info("사용자 찾음: {}", email);

            // 인증 토큰 생성 및 인증 시도
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, password);
            setDetails(request, authRequest);

            log.info("인증 시도 중...");
            Authentication authentication = authenticationManager.authenticate(authRequest);
            log.info("인증 완료: {}", authentication.isAuthenticated());

            return authentication;

        } catch (UsernameNotFoundException e) {
            log.error("사용자를 찾을 수 없음: {}", e.getMessage());
            throw e;
        } catch (AuthenticationException e) {
            log.error("인증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage()) {};
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) throws IOException {
        log.info("=== LoginFilter.successfulAuthentication 시작 ===");

        try {
            CustomMemberDetails customMemberDetails = (CustomMemberDetails) authResult.getPrincipal();
            Long memberId = customMemberDetails.getMemberId();

            log.info("토큰 생성 중. memberId: {}", memberId);

            String accessToken = jwtUtil.createAccessToken(memberId, "회원");
            String refreshToken = jwtUtil.createRefreshToken(memberId);

            // 리프레시 토큰 쿠키 설정
            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(refreshTokenCookie);

            // JSON 응답 설정
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("data", Map.of("accessToken", accessToken));

            String jsonResponse = new ObjectMapper().writeValueAsString(responseBody);
            response.getWriter().write(jsonResponse);

            log.info("로그인 성공 응답 전송 완료");

        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Internal server error during login\"}");
        }
    }
  
    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        log.error("=== LoginFilter.unsuccessfulAuthentication ===");
        log.error("Authentication failed: {}", failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Authentication failed: " + failed.getMessage());

        String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.info("에러 응답 전송 완료");
    }
}