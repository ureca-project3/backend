package com.triple.backend.common.config;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.repository.CommonCodeRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private final Key key;

    @Autowired
    private CommonCodeRepository commonCodeRepository;

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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    // JWT 토큰에서 role을 추출하는 메소드
    public String getRole(String token) {
        // 주어진 JWT 토큰에서 "role" 클레임을 추출
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    // JWT 토큰의 만료 여부를 확인하는 메소드
    public Boolean isExpired(String token) {
        // 주어진 JWT 토큰의 만료 시간을 확인하여 현재 시간과 비교
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    // 이메일 및 역할, 만료 기간을 포함한 JWT 토큰을 생성하는 메소드
    public String createJwt(String email, CommonCodeId roleCodeId, Long expiredMs) {
        // 역할 정보 가져오기
        CommonCode role = commonCodeRepository.findById(roleCodeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할 코드입니다."));

        // Claims 객체 생성
        Claims claims = Jwts.claims(); // Claims 생성

        claims.setSubject(email); // subject에 이메일을 저장
        claims.put("email", email); // 이메일 클레임 추가
        claims.put("role", role.getCommonName()); // 역할 클레임 추가 (commonName 사용)

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("생성된 JWT: " + jwt); // 생성된 JWT 출력
        return jwt;
    }
}
