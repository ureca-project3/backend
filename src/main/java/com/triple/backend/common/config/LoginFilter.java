package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.code.CommonCodeId;
import jakarta.servlet.FilterChain;
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
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        //클라이언트 요청에서 email, password 추출
        String email = obtainUsername(request);
        String password = obtainPassword(request);

        System.out.println(email);
        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null); // null이 아니라 role 값이 들어와야함

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        System.out.println("로그인 성공");

        // UserDetails
        CustomMemberDetails customMemberDetails = (CustomMemberDetails) authentication.getPrincipal();

        String email = customMemberDetails.getUsername(); // 이메일을 뽑아냄

        // 역할 코드 가져오기
        String roleCode = customMemberDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // GrantedAuthority에서 역할을 가져옴
                .findFirst() // 첫 번째 권한(역할)을 가져옴
                .orElseThrow(() -> new IllegalArgumentException("역할이 없습니다."));

        // CommonCodeId 생성
        CommonCodeId roleCodeId = new CommonCodeId(roleCode, "ROLE"); // roleCode가 그룹 ID로 사용될 수 있음

        // JWT 생성 (이메일과 역할 코드 사용)
        String token = jwtUtil.createJwt(email, roleCodeId, 60 * 60 * 10L);  // (email, roleCodeId, 60*60*10L)

        // 헤더에 Authorization JWT 데이터에 "Bearer " + token
        response.addHeader("Authorization", "Bearer " + token);
    }

    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("로그인 실패");

        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);

    }
}