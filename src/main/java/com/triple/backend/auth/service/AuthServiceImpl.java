package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.config.JWTUtil;
import com.triple.backend.common.repository.CommonCodeRepository;
import com.triple.backend.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.triple.backend.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final PasswordEncoder passwordEncoder; // 타입 변경
    private final MemberRepository memberRepository;
    private final CommonCodeRepository commonCodeRepository; // 공통코드 정보 찾기위한 기능
    private final JWTUtil jwtUtil; // JWTUtil 사용

    @Override
    public void joinProcess(JoinDto joinDto) {
        String memberName = joinDto.getMemberName();
        String email = joinDto.getEmail();
        String phone = joinDto.getPhone();
        String password = joinDto.getPassword();

        // 이메일 존재 여부 확인
        boolean isEmailExist = memberRepository.existsByEmail(email);

        // 이미 존재하면 종료
        if (isEmailExist) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        // 기본 역할 "010" (사용자) 가져오기
        CommonCodeId roleCodeId = new CommonCodeId("010", "100"); // 010(사용자), 100(회원가입)
        CommonCode role = commonCodeRepository.findById(roleCodeId)
                .orElseThrow(() -> new IllegalStateException("기본 역할을 찾을 수 없습니다.")); // 예외 처리

        // 회원가입 진행
        Member newMember = Member.builder()
                .name(memberName)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password)) // 비밀번호 암호화
                .role(role.getCommonName()) // 기본 역할 부여 (CommonCode에서 역할 이름을 가져오기)
                .build();

        memberRepository.save(newMember);
    }
}