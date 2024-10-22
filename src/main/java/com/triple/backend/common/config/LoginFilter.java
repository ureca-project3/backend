package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.code.CommonCodeId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    // 인증 처리 메소드
    private final AuthenticationManager authenticationManager;
    // JWTUtil 주입
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // 인증 정보를 나타내는 인터페이스. 로그인 기능
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        // "email" 값을 가져옴
        String email = request.getParameter("email");
        String password = obtainPassword(request);
        System.out.println("Username: " + email);
        System.out.println("Password: " + password);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password,
                null);

        return authenticationManager.authenticate(authToken);
    }



    // 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) {
        // 로그인 인증 정보를 가져옴 (CustomMemberDetails는 사용자 정의 UserDetails 구현체)
        CustomMemberDetails customMemberDetails = (CustomMemberDetails) authentication.getPrincipal();

        // 사용자 이름 (이메일 등)을 가져옴
        String username = customMemberDetails.getUsername();

        // 사용자의 권한 정보를 가져옴
        Collection<? extends GrantedAuthority> authorities = customMemberDetails.getAuthorities();

        // 권한 정보에서 첫 번째 권한을 가져옴 (하나 이상의 권한이 있을 수 있음)
        GrantedAuthority auth = authorities.iterator().next();

        // 첫 번째 권한의 이름을 가져옴
        String role = auth.getAuthority();

        // 사용자 ID를 가져옴 (CustomMemberDetails 클래스에서 정의된 메소드 추가)
        Long id = customMemberDetails.getMemberId(); // getMemberId 메소드 추가 필요

        // CommonCodeId 객체 생성 (role을 적절한 groupId와 함께 사용)
        String groupId = "100"; // 실제 그룹 ID로 바꿔야 함
        CommonCodeId roleCodeId = new CommonCodeId(role, groupId);

        // JWT Access Token 생성 (10시간 유효)
        String accessToken = jwtUtil.createJwt(username, roleCodeId, 60 * 60 * 10 * 1000L); // 10 hours in milliseconds

        // JWT Refresh Token 생성 (24시간 유효)
        String refreshToken = jwtUtil.createJwt(username, roleCodeId, 24 * 60 * 60 * 1000L); // 24 hours in milliseconds

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        Cookie cookie = new Cookie("Refresh-Token", refreshToken);
        cookie.setMaxAge(24 * 60 * 60); // 쿠키의 최대 수명 (1일)
        cookie.setHttpOnly(true); // 쿠키를 JavaScript에서 접근할 수 없도록 설정
        cookie.setSecure(true); // HTTPS에서만 쿠키를 전송하도록 설정 (SSL 사용 시)
        cookie.setPath("/"); // 쿠키의 유효 경로를 설정, /로 설정하면 모든 경로에서 접근 가능

        // 생성된 쿠키를 HTTP 응답에 추가
        response.addCookie(cookie); // 클라이언트는 이후의 요청에서 이 쿠키를 자동으로 포함하여 전송

        // Authorization 헤더에 Bearer 토큰을 추가
        response.addHeader("Authorization", "Bearer " + accessToken);

        // HTTP 응답 상태 코드를 200 OK로 설정
        response.setStatus(HttpServletResponse.SC_OK);
    }


    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("로그인 실패");

        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);

    }
}