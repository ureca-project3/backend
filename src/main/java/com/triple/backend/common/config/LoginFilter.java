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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    // 인증 처리 메소드
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    // JWTUtil 주입
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, MemberService memberService, JWTUtil jwtUtil) {

        this.authenticationManager = authenticationManager;
        this.memberService = memberService;
        this.jwtUtil = jwtUtil;
    }

    // 인증 정보를 나타내는 인터페이스. 로그인 기능
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String email = request.getParameter("email");  // 사용자의 이메일을 가지고옴
        String password = obtainPassword(request);

        // 사용자 정보를 데이터베이스에서 조회하여 역할 가져오기
        Member member = memberService.findByEmail(email); // 이메일 찾는 서비스
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (member != null && member.getRole() != null) {
            // 역할이 있는 경우 권한 추가
            String roleName = member.getRole().getId().getCodeId(); // 코드 ID를 사용하여 역할 이름 가져오기
            authorities.add(new SimpleGrantedAuthority(roleName));
        } else {
            // 역할이 없을 경우, 기본 역할 설정 가능 (예: 익명 사용자)
            authorities.add(new SimpleGrantedAuthority("역할 없음"));
        }

        // UsernamePasswordAuthenticationToken 생성 시 권한 목록 전달
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password, authorities);
        return authenticationManager.authenticate(authToken);
    }



    // 로그인 성공시 실행하는 메소드 (여기서 를 accessToken,refreshToken 발급하면 됨)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authentication) throws IOException {
        System.out.println("로그인 성공!!");
        CustomMemberDetails customMemberDetails = (CustomMemberDetails) authentication.getPrincipal();
        String username = customMemberDetails.getUsername();
        Collection<? extends GrantedAuthority> authorities = customMemberDetails.getAuthorities();
        GrantedAuthority auth = authorities.iterator().next();
        String role = auth.getAuthority();
        Long id = customMemberDetails.getMemberId();

        String groupId = "100"; // 실제 그룹 ID로 바꿔야 함
        CommonCodeId roleCodeId = new CommonCodeId(role, groupId);

        // JWT Access Token 생성 (10시간 유효)
        String accessToken = jwtUtil.createAccessToken(username, roleCodeId, 60 * 60 * 10 * 1000L);

        // JWT Refresh Token 생성 (24시간 유효)
        String refreshToken = jwtUtil.createRefreshToken(username, 24 * 60 * 60 * 1000L);

        // 리프레시 토큰을 HttpOnly 쿠키에 저장
        Cookie cookieR = new Cookie("Refresh-Token", refreshToken);
        cookieR.setMaxAge(24 * 60 * 60); // 쿠키의 최대 수명 (1일)
        cookieR.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
        cookieR.setSecure(true); // HTTPS에서만 쿠키를 전송하도록 설정
        cookieR.setPath("/"); // 쿠키의 유효 경로를 설정
        response.addCookie(cookieR); // 생성된 쿠키를 HTTP 응답에 추가

        // Access 토큰을 HttpOnly 쿠키에 저장
        Cookie cookieA = new Cookie("Access-Token", accessToken);
        cookieA.setMaxAge(24 * 60 * 60); // 쿠키의 최대 수명 (1일)
        cookieA.setHttpOnly(true); // JavaScript에서 접근할 수 없도록 설정
        cookieA.setSecure(true); // HTTPS에서만 쿠키를 전송하도록 설정
        cookieA.setPath("/"); // 쿠키의 유효 경로를 설정
        response.addCookie(cookieA); // 생성된 쿠키를 HTTP 응답에 추가

        // Authorization 해더에 accessToken 값 추가
        response.addHeader("Authorization", "Bearer " + accessToken);

        // JSON 응답 생성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Login Success");
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("accessToken", accessToken); // Access Token
        tokenData.put("refreshToken", refreshToken); // Refresh Token
        responseData.put("data", tokenData);
        responseData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        // JSON 응답 설정
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        // JSON 변환 및 출력
        ObjectMapper objectMapper = new ObjectMapper();
        PrintWriter out = response.getWriter();
        out.print(objectMapper.writeValueAsString(responseData));
        out.flush();

        // 로그인 성공 후 index.html로 리다이렉트
        //response.sendRedirect("/index.html"); // 리다이렉트 처리

    }



    // 로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        System.out.println("로그인 실패");

        //로그인 실패시 401 응답 코드 반환
        response.setStatus(401);

    }
}