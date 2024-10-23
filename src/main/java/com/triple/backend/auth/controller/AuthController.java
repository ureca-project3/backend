package com.triple.backend.auth.controller;

import com.triple.backend.auth.dto.CustomMemberDetails;
import com.triple.backend.auth.dto.LoginRequestDto;
import com.triple.backend.auth.dto.SignupRequestDto;
import com.triple.backend.auth.service.AuthService;
import com.triple.backend.common.dto.CommonResponse;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.common.config.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;

    // 일반 로그인 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signup(@RequestBody SignupRequestDto signupRequestDto) {
        authService.signup(signupRequestDto);
        return CommonResponse.ok("회원가입이 완료되었습니다.");
    }

    // 일반 로그인 API
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<String>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String token = String.valueOf(authService.login(loginRequestDto));
        response.setHeader("Authorization", "Bearer " + token);  // 로그인 성공 시 토큰 반환
        return CommonResponse.ok("로그인 성공", token);
    }

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
    public ResponseEntity<CommonResponse<String>> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("Authorization").substring(7);
        if (jwtUtil.validateToken(refreshToken)) {
            Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);
            String newAccessToken = jwtUtil.createAccessToken(memberId);
            return CommonResponse.ok("Access Token 재발급 성공", newAccessToken);
        }
        return CommonResponse.ok("Refresh Token이 유효하지 않습니다.");
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
    public ResponseEntity<CommonResponse<String>> logout() {
        // 로그아웃 로직 처리 (세션 무효화 또는 Refresh 토큰 삭제 등)
        return CommonResponse.ok("로그아웃 성공");
    }
}