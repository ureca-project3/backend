package com.triple.backend.auth.controller;

import com.triple.backend.common.config.JWTUtil;
import com.triple.backend.auth.entity.RefreshToken;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenController(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestHeader("Authorization") String refreshToken) {
        // Bearer 토큰 헤더에서 제거
        String token = refreshToken.substring(7);

        // Refresh 토큰 검증
        if (!jwtUtil.validateToken(token) || jwtUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Invalid or Expired Refresh Token");
        }

        // Refresh 토큰에서 memberId 추출
        Long memberId = jwtUtil.getMemberIdFromToken(token);

        // DB에서 해당 memberId의 유효한 Refresh 토큰이 있는지 확인
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByMemberId(memberId);
        if (optionalRefreshToken.isEmpty() || !optionalRefreshToken.get().getToken().equals(token)) {
            return ResponseEntity.status(401).body("Refresh Token not found or mismatch");
        }

        // 새로운 액세스 토큰 발급
        String newAccessToken = jwtUtil.createAccessToken(memberId);
        return ResponseEntity.ok(newAccessToken);
    }
}