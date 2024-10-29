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
import org.springframework.ui.Model;
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

    // 실제 로그인 처리를 위한 엔드포인트 추가
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(HttpServletRequest request) {
        // 실제 인증은 LoginFilter에서 처리
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public String joinProcess(JoinDto joinDto, RedirectAttributes redirectAttributes) {
        authService.joinProcess(joinDto);

        // 이메일 중복 확인
        if (authService.existsByEmail(joinDto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이메일이 이미 존재합니다.");
            return "redirect:/signup"; // 회원가입 페이지로 리다이렉트
        }

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

            // 5. 카카오 서비스 로그아웃
            String clientId = "ecbe8197ad00125d1d59da0fb88f4b3c";
            String logoutRedirectUri = "http://localhost:8080/auth/kakao-logout-callback";
            String kakaoLogoutUrl = String.format(
                    "https://kauth.kakao.com/oauth/logout?client_id=%s&logout_redirect_uri=%s",
                    clientId,
                    logoutRedirectUri
            );

            // 6. 카카오 로그아웃 페이지로 리다이렉트
            response.sendRedirect(kakaoLogoutUrl);
        } catch (Exception e) {
            log.error("Kakao logout error", e);
            response.sendRedirect("/index.html?error=logout_failed");
        }
    }

    @GetMapping("/kakao-logout-callback")
    public void kakaoLogoutCallback(HttpServletResponse response) throws IOException {
        // 카카오 로그아웃 후 최종적으로 우리 서비스의 index.html로 리다이렉트
        response.sendRedirect("/index.html");
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

    @GetMapping("/success")
    public String authSuccess(@RequestParam String accessToken, Model model) {
        // 토큰을 모델에 추가
        model.addAttribute("accessToken", accessToken);
        // auth-success.html 템플릿을 반환
        return "auth-success";
    }

}