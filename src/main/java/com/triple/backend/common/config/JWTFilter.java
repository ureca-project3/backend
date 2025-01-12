package com.triple.backend.common.config;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.common.exception.NotFoundException;
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
import java.util.Arrays;
import java.util.List;

// JWT 필터 검증
@AllArgsConstructor
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository; // MemberRepository 주입
    private final CommonCodeRepository commonCodeRepository;

    // Public API endpoints
    private static final List<String> PUBLIC_API_PATHS = Arrays.asList(
            "/books",               // 최신 도서 목록
            "/books/ranking",        // 인기 도서 목록
            "/event/api",
            "/auth",               // 인증 관련 API
            "/public"              // 공개 API
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Public API 요청은 인증 없이 통과
        if (isPublicApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

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
                        .orElseThrow(() -> NotFoundException.entityNotFound("Member"));

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
                // 수정된 부분: DB에서 member 조회하여 role 가져오기
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> NotFoundException.entityNotFound("Member"));
                String memberRole = member.getRole_code();  // 실제 필드명에 따라 수정 필요

                String newAccessToken = jwtUtil.createAccessToken(memberId, memberRole);

                // 새로운 Access Token을 응답 헤더에 추가
                response.setHeader("Authorization", "Bearer " + newAccessToken);

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

//        // 모든 토큰이 유효하지 않은 경우
//        if (isApiRequest(request)) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"message\": \"Unauthorized\"}");
//        } else {
//            response.sendRedirect("/login.html");
//        }

        // 인증이 필요한 API 요청이지만 토큰이 없는 경우
        if (isApiRequest(request) && !isPublicApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Unauthorized\"}");
        } else if (!"/".equals(path)) {
            response.sendRedirect("/login.html");
        } else {
            filterChain.doFilter(request, response);  // "/" 경로 필터링 통과
        }
    }

    private boolean isPublicApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_API_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/image/") ||
                path.startsWith("/books/") ||    // 모든 books 관련 경로 추가
                path.startsWith("/event/api/") ||
                path.matches(".*\\.(html|ico)$")|| // 모든 html 파일과 favicon.ico
                isPublicApiRequest(request);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void clearAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        String path = request.getRequestURI();
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Invalid token\"}");
        } else {
            response.sendRedirect("/login.html");
        }
    }
}