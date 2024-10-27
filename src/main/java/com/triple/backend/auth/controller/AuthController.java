package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.auth.service.AuthService;
import com.triple.backend.member.entity.Member;
import com.triple.backend.common.config.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html 파일을 반환합니다. (뷰 리졸버에 따라 경로 조정 필요)
    }

    @PostMapping("/signup")
    public String joinProcess(JoinDto joinDto, RedirectAttributes redirectAttributes) {
        authService.joinProcess(joinDto);

        // 회원가입 성공 메시지 추가 (필요에 따라 수정 가능)
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다!");

        // index.html로 리다이렉트
        return "redirect:/index.html";
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
            SecurityContextHolder.clearContext();

            // 5. 리다이렉트
            response.sendRedirect("/index.html");
        } catch (Exception e) {
            log.error("Kakao logout error", e);
            response.sendRedirect("/index.html?error=logout_failed");
        }
    }

    @GetMapping("/token/access")
    public ResponseEntity<Map<String, String>> getAccessToken(HttpServletRequest request) {
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
            String newAccessToken = jwtUtil.createAccessToken(memberId);

            // 액세스 토큰을 JSON 형태로 반환
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}