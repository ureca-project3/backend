package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

// JWT 필터 검증
@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository; // MemberRepository 주입

//    public JWTFilter(JWTUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더 로깅 ( Authorization 헤더가 포함되어 있는지 확인 )
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        System.out.println("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 시간 검증
        if (jwtUtil.isTokenExpired(token)) {
            System.out.println("token expired");
            filterChain.doFilter(request, response);
            //조건이 해당되면 메소드 종료 (필수)
            return;
        }
//        // 토큰에서 email과 role 획득, role 사용안함
//        String email = jwtUtil.getEmail(token);
//        // JWT에서 memberId 추출
//        Long memberId = jwtUtil.getMemberIdFromToken(token);
//        // 이메일 조회
//        String email = jwtUtil.getEmail(memberId);
//        String role = jwtUtil.getRole(token);
//        //userEntity를 생성하여 값 set
//        Member member = new Member();
////        member.setName(email);
//        member.setPassword("temppassword"); // 임시적으로 비밀번호 입력
////        member.setRole(role);

        // 토큰에서 memberId 추출
        Long memberId = jwtUtil.getMemberIdFromToken(token);

        // memberId로 DB에서 사용자 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            System.out.println("Member not found");
            filterChain.doFilter(request, response);
            return; // 사용자 정보가 없으면 메소드 종료
        }


        //UserDetails에 회원 정보 객체 담기
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }
}