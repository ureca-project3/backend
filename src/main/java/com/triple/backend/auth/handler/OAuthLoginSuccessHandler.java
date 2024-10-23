package com.triple.backend.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.auth.entity.RefreshToken;
import com.triple.backend.auth.repository.RefreshTokenRepository;
import com.triple.backend.common.config.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${spring.jwt.redirect}")
    private String REDIRECT_URI;

    @Value("${spring.jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${spring.jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    private final JWTUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        final String provider = token.getAuthorizedClientRegistrationId(); // Provider 추출 (kakao)

        Map<String, Object> attributes = token.getPrincipal().getAttributes();
        // 소셜 제공자에서 제공한 provider ID
        Long providerIdLong = (Long) attributes.get("id");
        String providerId = String.valueOf(providerIdLong); // providerId를 String으로 변환

        // 사용자 정보 추출 (이메일, 성함, 전화번호)
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String phone = (String) kakaoAccount.get("phone_number");
        String name = (String) kakaoAccount.get("name");

        // ProviderId로 사용자 찾기
        Member existMember = memberRepository.findByProviderId(providerId);
        Member member;

        if (existMember == null) {
            // 신규 유저 처리
            log.info("신규 유저입니다. 등록을 진행합니다.");

            member = Member.builder()
                    .name(name)
                    .email(email)
                    .phone(phone)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(member);
        } else {
            // 기존 유저 처리
            log.info("기존 유저입니다.");
            refreshTokenRepository.deleteByMemberId(existMember.getMemberId());
            member = existMember;
        }

        log.info("유저 이름 : {}", name);
        log.info("PROVIDER : {}", provider);
        log.info("PROVIDER_ID : {}", providerId);

        // 리프레시 토큰 생성 및 저장
        String refreshToken = jwtUtil.createRefreshToken(member.getMemberId());
        RefreshToken newRefreshToken = RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME), ZoneId.systemDefault()))  // 만료 시간 설정
                .build();
        refreshTokenRepository.save(newRefreshToken);

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getMemberId());

        // 이름, 액세스 토큰, 리프레쉬 토큰을 담아 리다이렉트
        String encodedName = URLEncoder.encode(name, "UTF-8");
        String redirectUri = String.format(REDIRECT_URI, encodedName, accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
        log.info("Redirecting to: {}", redirectUri);
    }
}