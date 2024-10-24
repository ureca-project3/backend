package com.triple.backend.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private final Key key;

    // Access Token과 Refresh Token의 만료 시간을 properties에서 가져옴
    @Value("${spring.jwt.access-token.expiration-time}")
    private Long accessTokenExpirationMillis;

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private Long refreshTokenExpirationMillis;


    // secret 값을 Base64 디코딩하여 Key로 변환하는 생성자
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(byteSecretKey);   // 서명키를 한번만 생성
    }

    // Access Token 생성 메서드
    public String createAccessToken(Long memberId) {
        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);  // memberId만 저장

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMillis))  // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)  // 서명 알고리즘과 키
                .compact();  // 토큰 생성
    }

    // Refresh Token 생성 메서드
    public String createRefreshToken(Long memberId) {
        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);  // Refresh Token에 사용할 memberId

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMillis))  // 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)  // 서명 알고리즘과 키
                .compact();
    }

    // JWT 토큰에서 memberId를 추출하는 메서드
    public Long getMemberIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.get("memberId", Long.class);  // memberId를 추출
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new RuntimeException("Invalid token");
        }
    }

    // 이메일, role 및 만료 기간을 포함한 JWT 토큰을 생성하는 메소드
    public String createJwt(String email, Long expiredMs) {   // String role 제거
        // 클레임에 email과 role을 추가
        Claims claims = Jwts.claims();
        claims.put("email", email);  // email로 클레임 수
//      claims.put("role", role);

        // JWT 토큰을 생성하고 서명한 후 반환
        return Jwts.builder()
                .setClaims(claims)  // 클레임 설정
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))  // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS256)  // 서명 알고리즘과 키를 사용하여 서명
                .compact();  // 토큰 생성
    }

    // 토큰 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 검증에 실패했습니다.");
            return false;
        }
    }

    // 토큰의 만료 여부를 확인하는 메서드
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
            return expirationDate.before(new Date());  // 현재 날짜와 만료 날짜 비교
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다.");
            throw new RuntimeException("Invalid token");
        }
    }

    // 쿠키에서 액세스 토큰 추출
    public String extractAccessToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, "accessToken");
    }

    // 쿠키에서 리프레시 토큰 추출
    public String extractRefreshToken(HttpServletRequest request) {
        return extractTokenFromCookies(request, "refreshToken");
    }

    private String extractTokenFromCookies(HttpServletRequest request, String tokenName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(tokenName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // 응답 헤더에서 토큰 추출
    public String getTokenFromHeader(String authorizationHeader) {
        return authorizationHeader.substring(7);  // "Bearer " 제거 후 토큰 반환
    }

}