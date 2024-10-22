package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.repository.CommonCodeRepository;
import com.triple.backend.member.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Optional;

// JWT 필터 검증
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CommonCodeRepository commonCodeRepository;
    public JWTFilter(JWTUtil jwtUtil, CommonCodeRepository commonCodeRepository) {
        this.jwtUtil = jwtUtil;
        this.commonCodeRepository = commonCodeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        // Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("Authorization 헤더가 존재하지 않거나 Bearer 토큰이 아닙니다.");
            filterChain.doFilter(request, response);

            // 조건이 해당되면 메소드 종료 (필수)
            return;
        }

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            System.out.println("토큰이 만료되었습니다.");
            filterChain.doFilter(request, response);

            // 조건이 해당되면 메소드 종료 (필수)
            return;
        }


        // 토큰에서 email 획득
        String email = jwtUtil.getEmail(token);

        // userEntity를 생성하여 값 set
        Member member = new Member();
        member.setName(email);

        // UserDetails에 회원 정보 객체 담기
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
        System.out.println("Authentication 토큰 생성 완료");

        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("사용자 세션 등록 완료");

        // 다음 필터로 넘김
        filterChain.doFilter(request, response);
        System.out.println("JWTFilter완료");
    }

}