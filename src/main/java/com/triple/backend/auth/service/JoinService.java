package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.repository.CommonCodeRepository;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.triple.backend.common.config.JWTUtil;

@Service
public class JoinService {

    private final MemberRepository memberRepository;

    // 비밀번호 가입시 암호화

    private final PasswordEncoder passwordEncoder; // 타입 변경
    private final CommonCodeRepository commonCodeRepository; // 공통코드 정보 찾기위한 기능
    private final JWTUtil jwtUtil; // JWTUtil 사용
    // 생성자 초기화
    public JoinService(MemberRepository memberRepository, CommonCodeRepository commonCodeRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.commonCodeRepository = commonCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }


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
        Member newMember = new Member();
        newMember.setName(memberName);
        newMember.setEmail(email);
        newMember.setPhone(phone);
        // 비밀번호 암호화
        newMember.setPassword(passwordEncoder.encode(password));
        newMember.setRole(role); // 기본역할 부여
        memberRepository.save(newMember);

        // JWT 생성 (예시: 만료 시간 1시간)
        String token = jwtUtil.createJwt(email, roleCodeId, 3600000L); // 이메일과 역할 코드 ID 사용
        // 생성된 JWT를 클라이언트에 반환하거나 로그 등을 통해 확인할 수 있음
        System.out.println("생성된 JWT: " + token);
    }
}