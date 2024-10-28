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

// JWT 필터 검증
@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository; // MemberRepository 주입
    private final CommonCodeRepository commonCodeRepository;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        // request에서 Authorization 헤더를 찾음
//        String authorization = request.getHeader("Authorization");
//
//        //Authorization 헤더 검증
//        if (authorization == null || !authorization.startsWith("Bearer ")) {
//            System.out.println("token null");
//            filterChain.doFilter(request, response);
//            //조건이 해당되면 메소드 종료 (필수)
//            return;
//        }
//
//        // Bearer 부분 제거 후 순수 토큰만 획득
//        String token = authorization.split(" ")[1];
//        System.out.println("토큰 추출 완료: " + token);
//
//        // 토큰 소멸 시간 검증
//        if (jwtUtil.isExpired(token)) {
//            System.out.println("토큰이 만료되었습니다.");
//            filterChain.doFilter(request, response);
//
//            // 조건이 해당되면 메소드 종료 (필수)
//            return;
//        }
//
//        // 토큰에서 email과 role 획득
//        String email = jwtUtil.getEmail(token);
//        // MemberEntity를 생성하여 값 set
//        Member member = new Member();
//        member.setName(email);// 이름을 이메일로,...?
//
//        // 토큰에서 memberId 추출
//        Long memberId = jwtUtil.getMemberIdFromToken(token);
//
//        // memberId로 DB에서 사용자 조회
//        member = memberRepository.findById(memberId).orElse(null);
//        if (member == null) {
//            System.out.println("Member not found");
//            filterChain.doFilter(request, response);
//            return; // 사용자 정보가 없으면 메소드 종료
//        }
//
//        //UserDetails에 회원 정보 객체 담기
//        CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
//        //스프링 시큐리티 인증 토큰 생성
//        Authentication authToken = new UsernamePasswordAuthenticationToken(customMemberDetails, null, customMemberDetails.getAuthorities());
//        //세션에 사용자 등록
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//        System.out.println("사용자 세션 등록 완료");
//        // 다음 필터로 요청 전달
//        filterChain.doFilter(request, response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 인증이 필요없는 경로는 바로 통과
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Access Token 확인
        String authorization = request.getHeader("Authorization");
        String accessToken = null;

        if (authorization != null && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        // SessionStorage에서 토큰 확인
        if (accessToken == null && request.getSession() != null) {
            accessToken = (String) request.getSession().getAttribute("accessToken");
        }

        // Access Token 검증
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            try {
                Long memberId = jwtUtil.getMemberIdFromToken(accessToken);
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member not found"));

                CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
                Authentication authToken = new UsernamePasswordAuthenticationToken(
                        customMemberDetails,
                        null,
                        customMemberDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                // 토큰은 있지만 처리 중 오류 발생
                clearAuthentication(request, response);
                return;
            }
        }

        // Access Token이 없거나 유효하지 않은 경우 Refresh Token 확인
        String refreshToken = jwtUtil.extractRefreshToken(request);

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            try {
                Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);
                String newAccessToken = jwtUtil.createAccessToken(memberId);

                // 새로운 Access Token을 응답 헤더에 추가
                response.setHeader("Authorization", "Bearer " + newAccessToken);

                // 인증 처리
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member not found"));

                CustomMemberDetails customMemberDetails = new CustomMemberDetails(member);
                Authentication authToken = new UsernamePasswordAuthenticationToken(
                        customMemberDetails,
                        null,
                        customMemberDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                clearAuthentication(request, response);
                return;
            }
        }

        // 모든 토큰이 유효하지 않은 경우
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
        } else {
            response.sendRedirect("/login.html");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/image/") ||
                path.equals("/favicon.ico") ||
                path.equals("/login.html") ||
                path.equals("/signup.html") ||
                path.equals("/index.html");
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void clearAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Invalid token\"}");
        } else {
            response.sendRedirect("/login.html");
        }
    }
}