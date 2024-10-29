package com.triple.backend.child.repository;

import com.triple.backend.child.entity.Child;
import com.triple.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    // member로 자녀 목록 조회
    List<Child> findAllByMember(Member member);
}