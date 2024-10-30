package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.auth.service.AuthService;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.common.dto.ErrorResponse;
import com.triple.backend.member.entity.Member;
import com.triple.backend.common.config.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.Cookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakao_client_id;

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;

    // 실제 로그인 처리를 위한 엔드포인트 추가
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<Void>> login(HttpServletRequest request) {
        // 실제 인증은 LoginFilter에서 처리
        return CommonResponse.ok("로그인이 성공적으로 완료되었습니다.");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> joinProcess(@RequestBody JoinDto joinDto) {  // @RequestBody 추가
        try {
            authService.joinProcess(joinDto);
            return CommonResponse.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.error(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    @GetMapping("/api/member/kakao-logout")
    public ResponseEntity<?> kakaoLogoutRedirect(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            Authentication authentication
    ) throws IOException {
        try {
            // 1. DB에서 RefreshToken 삭제
            if (authentication != null && authentication.getPrincipal() instanceof Member) {
                Member member = (Member) authentication.getPrincipal();
                refreshTokenRepository.deleteByMemberId(member.getMemberId());
            }

            // 2. RefreshToken 쿠키 삭제
            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setMaxAge(0);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            response.addCookie(refreshTokenCookie);

            // 3. 세션 무효화
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            // JSESSIONID 쿠키 삭제
            Cookie sessionCookie = new Cookie("JSESSIONID", null);
            sessionCookie.setMaxAge(0);
            sessionCookie.setPath("/");
            response.addCookie(sessionCookie);

            // 4. Security Context 정리
            SecurityContextHolder.clearContext();

            // 5. 카카오 서비스 로그아웃
            String clientId = kakao_client_id;
            String logoutRedirectUri = "http://localhost:8080/auth/kakao-logout-callback";
            String kakaoLogoutUrl = String.format(
                    "https://kauth.kakao.com/oauth/logout?client_id=%s&logout_redirect_uri=%s",
                    clientId,
                    logoutRedirectUri
            );

            // 6. 카카오 로그아웃 페이지로 리다이렉트
            response.sendRedirect(kakaoLogoutUrl);
            return CommonResponse.ok("카카오 서버에서 로그아웃이 성공적으로 처리되었습니다.");

        } catch (Exception e) {
            log.error("Kakao logout error", e);
            response.sendRedirect("/index.html?error=logout_failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/kakao-logout-callback")
    public ResponseEntity<?> kakaoLogoutCallback(HttpServletResponse response) throws IOException {
        // 카카오 로그아웃 후 최종적으로 우리 서비스의 index.html로 리다이렉트
        response.sendRedirect("/index.html");
        return CommonResponse.ok("카카오 로그아웃 처리가 완료되었습니다.");
    }

    @GetMapping("/token/access")
    public ResponseEntity<CommonResponse<Map<String, String>>> getAccessToken(HttpServletRequest request) {
        // 쿠키가 null인지 먼저 확인
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RuntimeException("No cookies found in the request");
        }

        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("No refresh token found"));

        // 리프레시 토큰을 검증하고 새로운 액세스 토큰 발급
        if (jwtUtil.validateToken(refreshToken)) {
            Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);
            String newAccessToken = jwtUtil.createAccessToken(memberId,role);

            // 액세스 토큰을 JSON 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return CommonResponse.ok("새로운 액세스 토큰이 발급되었습니다.", response);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }

    }
}