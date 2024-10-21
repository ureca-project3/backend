package com.triple.backend.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

    private final Key key;

    // secret 값을 Base64 디코딩하여 Key로 변환하는 생성자
    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        // Base64로 인코딩된 secret 키를 디코딩하여 byte 배열로 변환
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        // HMAC-SHA 기반의 Key를 생성
        this.key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    // JWT 토큰에서 이메일을 추출하는 메소드
    public String getEmail(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("email", String.class);
    }

    // JWT 토큰에서 role을 추출하는 메소드, 하지만 role은 우리 프로젝트에 사용하지 않음
    public String getRole(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    // JWT 토큰의 만료 여부를 확인하는 메소드
    public Boolean isExpired(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
    }

    // 이메일 및 만료 기간을 포함한 JWT 토큰을 생성하는 메소드
    public String createJwt(String email, Long expiredMs) {
        Claims claims = Jwts.claims();
        claims.put("email", email);  // email 클레임 추가

        // JWT 토큰을 생성하고 서명한 후 반환
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();  // 토큰 생성

        System.out.println("Generated JWT: " + jwt); // JWT를 출력하여 확인
        return jwt;
    }

}
