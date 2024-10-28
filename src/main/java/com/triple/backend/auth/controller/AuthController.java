package com.triple.backend.auth.controller;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.auth.dto.LoginRequestDto;
import com.triple.backend.auth.dto.SignupRequestDto;
import com.triple.backend.auth.dto.TokenResponseDto;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.auth.service.AuthService;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.common.dto.ErrorResponse;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.common.config.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.Cookie;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 일반 로그인 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signup(@RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return CommonResponse.ok("회원가입이 완료되었습니다.");
    }

//    // 일반 로그인 API
//    @PostMapping("/login")
//    public ResponseEntity<CommonResponse<String>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
//        String token = String.valueOf(authService.login(loginRequestDto));
//        response.setHeader("Authorization", "Bearer " + token);  // 로그인 성공 시 토큰 반환
//        return CommonResponse.ok("로그인 성공", token);
//    }

    // 소셜 로그인 카카오 API
    @GetMapping("/login/oauth2/kakao")
    public ResponseEntity<CommonResponse<String>> kakaoLogin(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String provider = token.getAuthorizedClientRegistrationId();

        if ("kakao".equals(provider)) {
            log.info("카카오 로그인 성공");
            // JWT 발급 및 처리 로직 추가 가능
            return CommonResponse.ok("카카오 로그인 성공");
        }

        return CommonResponse.ok("소셜 로그인 실패");
    }

    // 토큰 갱신 API (Access 토큰 재발급)
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = Arrays.stream(request.getCookies())
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
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 이 API는 인증된 사용자만 접근 가능
    @GetMapping("/user-info")
    public ResponseEntity<String> getUserInfo(@AuthenticationPrincipal CustomMemberDetails memberDetails) {
        String username = memberDetails.getUsername();
        String email = memberDetails.getUserPhone();
        String phone = memberDetails.getUseremail();

        String responseMessage = "안녕하세요 " + username + "님";
        String userInfo = "Name: " + username + ", Email: " + email + ", Phone: " + phone;

        return ResponseEntity.ok(userInfo + "\n" + responseMessage);
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Authorization 헤더에서 액세스 토큰 추출
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 에러 응답: 액세스 토큰이 없을 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "액세스 토큰이 없습니다."));
        }

        String accessToken = authHeader.substring(7); // "Bearer " 제거하고 토큰만 추출

        // 액세스 토큰 유효성 검사
        if (!jwtUtil.validateToken(accessToken)) {
            // 에러 응답: 유효하지 않은 액세스 토큰일 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."));
        }

        // 토큰에서 사용자 ID 추출
        Long memberId = jwtUtil.getMemberIdFromToken(accessToken);

        // 리프레시 토큰 삭제 (로그아웃 처리)
        refreshTokenRepository.deleteByMemberId(memberId);

        log.info(accessToken);
        // 로그아웃 성공 응답
        return CommonResponse.ok("로그아웃 성공");
    }

    @GetMapping("/api/member/kakao-logout")
    public void kakaoLogoutRedirect(
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
//            SecurityContextHolder.clearContext();

            // 5. 리다이렉트
            response.sendRedirect("/index.html?logout=success");
        } catch (Exception e) {
            log.error("Kakao logout error", e);
            response.sendRedirect("/index.html?error=logout_failed");
        }
    }

    @GetMapping("/token/access")
    public ResponseEntity<Map<String, String>> getAccessToken(HttpServletRequest request) {
        System.out.println("/token/access 에 들어옴");

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
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}