package com.triple.backend.child.service.impl;

import com.triple.backend.child.dto.ChildRegisterRequestDto;
import com.triple.backend.child.repository.ChildRepository;
import com.triple.backend.child.service.ChildService;
import com.triple.backend.member.entity.Member;
import com.triple.backend.child.entity.Child;
import com.triple.backend.member.repository.MemberRepository;
import com.triple.backend.common.config.JWTUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChildServiceImpl implements ChildService {
    private final ChildRepository childRepository;
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Override
    public void registerChild(ChildRegisterRequestDto request, String accessToken) {
        Long memberId = extractMemberIdFromToken(accessToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        Child child = new Child();
        child.setMember(member);
        child.setName(request.getName());
        child.setBirthdate(request.getBirthDate());
        child.setGender(request.getGender().equals("F") ? "여자" : "남자");
        child.setImageUrl(request.getProfileImage());
        child.setAge(request.getAge());

        childRepository.save(child);
    }

    // JWT 토큰에서 memberId 추출
    private Long extractMemberIdFromToken(String accessToken) {
        long memberId = jwtUtil.getMemberIdFromToken(accessToken);
        return memberId;
    }
}
