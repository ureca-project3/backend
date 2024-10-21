package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em; // EntityManager를 사용하여 CommonCode 조회

    // 비밀번호 가입시 암호화
    private final PasswordEncoder passwordEncoder; // 타입 변경

    // 생성자 초기화
    public JoinService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder; // 초기화
    }


    public void joinProcess(JoinDto joinDto){
        String memberName = joinDto.getMemberName();
        String email = joinDto.getEmail();
        String phone = joinDto.getPhone();
        String password = joinDto.getPassword();

        // 이메일 존재 여부 확인
        boolean isEmailExist = memberRepository.existsByEmail(email);

        // 공통코드에서 역할 조회
        CommonCodeId roleCodeId = new CommonCodeId("010", "100"); // 사용자(010) 회원가입(100) 기본설정
        CommonCode commonCode = em.find(CommonCode.class, roleCodeId);

        // 이미 존재하면 종료
        if (isEmailExist){
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 회원가입 진행
        Member newMember = new Member();
        newMember.setName(memberName);
        newMember.setEmail(email);
        newMember.setPhone(phone);
        // 비밀번호 암호화
        newMember.setPassword(passwordEncoder.encode(password));
        newMember.setRole(commonCode); // 공통코드에서 역할 설정
        memberRepository.save(newMember);

    }
}
