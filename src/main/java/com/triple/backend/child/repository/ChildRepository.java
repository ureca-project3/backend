package com.triple.backend.child.repository;

import com.triple.backend.child.entity.Child;
import com.triple.backend.member.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    // member로 자녀 목록 조회
    List<Child> findAllByMember(Member member);

    boolean existsByMember(Member member);
    @Transactional
    void deleteByMember(Member member);

    // 특정 자녀삭제
    @Transactional
    void deleteByChildIdAndMember(Long childId, Member member);

    boolean existsByChildIdAndMember(Long childId, Member member);
}