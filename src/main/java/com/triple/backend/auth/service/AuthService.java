package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.LoginRequestDto;
import com.triple.backend.auth.dto.SignupRequestDto;
import com.triple.backend.auth.dto.TokenResponseDto;
import com.triple.backend.auth.entity.RefreshToken;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.common.config.JWTUtil;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // 일반 회원가입
    public void signup(SignupRequestDto signupRequestDto) {
        if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        Member newMember = Member.builder()
                .email(signupRequestDto.getEmail())
                .name(signupRequestDto.getName())
                .password(passwordEncoder.encode(signupRequestDto.getPassword())) // 비밀번호 암호화
                .provider("email")
                .build();

        memberRepository.save(newMember);
    }

//    // 일반 로그인
//    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
//        Member member = memberRepository.findByEmail(loginRequestDto.getEmail());
//        if(member == null) {
//            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
//        }
//
//        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
//            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
//        }
//
//        // Access Token 및 Refresh Token 생성
//        String accessToken = jwtUtil.createAccessToken(member.getMemberId());
//        String refreshToken = jwtUtil.createRefreshToken(member.getMemberId());
//
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME), ZoneId.systemDefault());
//
//        // Refresh Token 저장
//        refreshTokenRepository.save(new RefreshToken(refreshToken, member, localDateTime));
//        // 리프레시 토큰 저장
////        RefreshToken newRefreshToken = RefreshToken.builder()
////                .member(member)
////                .token(refreshToken)
////                .expiryDate(localDateTime)  // 만료 시간 설정
////                .build();
////        refreshTokenRepository.save(newRefreshToken);
//
//
//        return new TokenResponseDto(accessToken, refreshToken);
//    }

//    // 소셜 로그인 (카카오)
//    public TokenResponseDto socialLogin(String authorizationHeader) {
//        String kakaoAccessToken = authorizationHeader.substring(7); // Bearer 제거
//
//        // 카카오 API로 providerId 획득 (providerId는 카카오에서 받음)
//        String providerId = getKakaoProviderId(kakaoAccessToken); // 카카오에서 받아온 providerId로 진행
//
//        Member member = memberRepository.findByProviderId(providerId)
//        if(member == null) {
//            // 신규 유저 처리
//            Member newMember = Member.builder()
//                    .provider("kakao")
//                    .providerId(providerId)
//                    .build();
//            return memberRepository.save(newMember);
//        }
//
//        // Access Token 및 Refresh Token 생성
//        String accessToken = jwtUtil.createAccessToken(member.getMemberId());
//        String refreshToken = jwtUtil.createRefreshToken(member.getMemberId());
//
//        // Refresh Token 저장
//        refreshTokenRepository.save(new RefreshToken(member, refreshToken));
//
//        return new TokenResponseDto(accessToken, refreshToken);
//    }
//
//    // 액세스 토큰 재발행
//    public TokenResponseDto reissueAccessToken(String refreshToken) {
//        // Refresh Token 검증
//        if (!jwtUtil.validateToken(refreshToken)) {
//            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
//        }
//
//        Long memberId = jwtUtil.getMemberIdFromToken(refreshToken);
//
//        // 새로운 Access Token 생성
//        String newAccessToken = jwtUtil.createAccessToken(memberId);
//
//        return new TokenResponseDto(newAccessToken, refreshToken);
//    }

    // 로그아웃
    public void logout(String accessToken) {
        Long memberId = jwtUtil.getMemberIdFromToken(accessToken);
        refreshTokenRepository.deleteByMemberId(memberId);
    }

    // 카카오에서 providerId를 받아오는 메서드 (카카오 API와 통신)
    private String getKakaoProviderId(String kakaoAccessToken) {
        // 카카오 API로 카카오 계정 정보에서 providerId 획득하는 로직
        return "kakao-provider-id"; // 예시로 반환
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    public TokenResponseDto refreshAccessToken(HttpServletRequest request) {
        String refreshToken = jwtUtil.extractRefreshToken(request);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long memberId = jwtUtil.getMemberIdFromToken(refreshToken); // id 추출
        String memberRole = jwtUtil.getRoleFromToken(refreshToken); // 역할 추출
        String newAccessToken = jwtUtil.createAccessToken(memberId,memberRole);

        return new TokenResponseDto(newAccessToken, refreshToken); // 리프레시 토큰은 유지
    }

}