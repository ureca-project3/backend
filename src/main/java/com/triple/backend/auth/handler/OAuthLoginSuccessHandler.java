package com.triple.backend.auth.handler;

import jakarta.servlet.http.Cookie;
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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
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
    private final OAuth2AuthorizedClientService authorizedClientService;

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

        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = jwtUtil.createRefreshToken(member.getMemberId());
        RefreshToken newRefreshToken = RefreshToken.builder()
                .member(member)
                .token(refreshToken)
                .expiryDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_TIME), ZoneId.systemDefault()))  // 만료 시간 설정
                .build();
        refreshTokenRepository.save(newRefreshToken);

        // 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member.getMemberId());

        // 리프레시 토큰을 쿠키에 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);  // 자바스크립트로 접근 불가
        refreshTokenCookie.setSecure(false);  // HTTPS 연결에서만 전송
        refreshTokenCookie.setMaxAge((int) REFRESH_TOKEN_EXPIRATION_TIME / 1000);  // 만료 시간 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setDomain("localhost"); // 쿠키 도메인 설정
        response.addCookie(refreshTokenCookie);

        // 액세스 토큰은 헤더에 추가
        response.setHeader("Authorization", "Bearer " + accessToken);


        log.info("Creating Access Token for memberId: {}",member.getMemberId());
        log.info("Created Access Token: {}", accessToken);

        log.info("Creating Refresh Token for memberId: {}", member.getMemberId());
        log.info("Created Refresh Token: {}", refreshToken);

        // index2.html로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, "/index.html");
    }
}