package com.triple.backend.auth.service;

import com.triple.backend.auth.dto.JoinDto;
import com.triple.backend.member.entity.Member;
import com.triple.backend.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final MemberRepository memberRepository;

    // 비밀번호 가입시 암호화
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 생성자 초기화
    public JoinService(MemberRepository memberRepository, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void joinProcess(JoinDto joinDto){
        String memberName = joinDto.getMemberName();
        String email = joinDto.getEmail();
        String phone = joinDto.getPhone();
        String password = joinDto.getPassword();

        // 이메일 존재 여부 확인
        boolean isEmailExist = memberRepository.existsByEmail(email);


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
        newMember.setPassword(bCryptPasswordEncoder.encode(password));

        memberRepository.save(newMember);

    }
}
