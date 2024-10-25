package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.repository.CommonCodeRepository;
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
import java.util.List;
import java.util.Optional;

// JWT 필터 검증
@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository; // MemberRepository 주입
    private final CommonCodeRepository commonCodeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//// 요청 헤더 로깅 ( Authorization 헤더가 포함되어 있는지 확인 )
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            System.out.println(headerName + ": " + request.getHeader(headerName));
//        }
        // request에서 Authorization 헤더를 찾음
        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            //조건이 해당되면 메소드 종료 (필수)
            return;
        }

        // Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];
        System.out.println("토큰 추출 완료: " + token);

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            System.out.println("토큰이 만료되었습니다.");
            filterChain.doFilter(request, response);

            // 조건이 해당되면 메소드 종료 (필수)
            return;
        }
        // 토큰에서 email과 role 획득
        String email = jwtUtil.getEmail(token);
        // MemberEntity를 생성하여 값 set
        Member member = new Member();
        member.setName(email);

        // 토큰에서 memberId 추출
        Long memberId = jwtUtil.getMemberIdFromToken(token);

        // memberId로 DB에서 사용자 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) {
            System.out.println("Member not found");
            filterChain.doFilter(request, response);
            return; // 사용자 정보가 없으면 메소드 종료
        }

        // 쿠키에서 액세스 토큰 추출
        String token = jwtUtil.extractAccessToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long memberId = jwtUtil.getMemberIdFromToken(token);

            // 인증 객체를 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(memberId, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        //UserDetails에 회원 정보 객체 담기
        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("사용자 세션 등록 완료");
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);

    }
}