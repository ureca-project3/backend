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
        byte[] byteSecretKey = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(byteSecretKey);
    }

    // JWT 토큰에서 이메일을 추출하는 메소드
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    // JWT 토큰의 만료 여부를 확인하는 메소드
    public Boolean isExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public String createJWTToken(String email, CommonCodeId roleCodeId, Long expiredMs) {
        return createJwt(email, roleCodeId, expiredMs, "JWT");
    }
    // 이메일 및 역할, 만료 기간을 포함한 Access JWT 토큰을 생성하는 메소드
    public String createAccessToken(String email, CommonCodeId roleCodeId, Long expiredMs) {
        return createJwt(email, roleCodeId, expiredMs, "access");
    }

    // 이메일 및 역할, 만료 기간을 포함한 Refresh JWT 토큰을 생성하는 메소드
    public String createRefreshToken(String email, Long expiredMs) {
        return createJwt(email, null, expiredMs, "refresh"); // 사용자의 역할 정보가 필요하지 않으므로 null
    }

    // 이메일 및 역할, 만료 기간을 포함한 JWT 토큰을 생성하는 메소드
    private String createJwt(String email, CommonCodeId roleCodeId, Long expiredMs, String tokenType) {
        // 역할 정보 가져오기
        CommonCode role = null;
        if (roleCodeId != null) {
            role = commonCodeRepository.findById(roleCodeId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 역할 코드입니다."));
        }

        // Claims 객체 생성
        Claims claims = Jwts.claims();
        claims.setSubject(email);
        claims.put("email", email);
        if (role != null) {
            claims.put("role", role.getCommonName());
        }
        claims.put("tokenType", tokenType); // 토큰 유형 추가

        String jwt = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("생성된 "+ tokenType + "Token : " + jwt );
        return jwt;
    }
}
