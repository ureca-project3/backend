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
        // 주어진 JWT 토큰에서 "email" 클레임을 추출
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("email", String.class);
    }

    // JWT 토큰에서 role을 추출하는 메소드, 하지만 role은 우리 프로젝트에 사용하지 않음
    public String getRole(String token) {
        // 주어진 JWT 토큰에서 "role" 클레임을 추출
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("role", String.class);
    }

    // JWT 토큰의 만료 여부를 확인하는 메소드
    public Boolean isExpired(String token) {
        // 주어진 JWT 토큰의 만료 시간을 확인하여 현재 시간과 비교
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
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
}
