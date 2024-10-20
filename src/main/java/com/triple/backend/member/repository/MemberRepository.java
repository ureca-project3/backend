package com.triple.backend.member.repository;


import com.triple.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    // 해당 유저 조회
    Member findByEmail(String email);

    // email이 이미 존재하는지 확인(중복체크)
    Boolean existsByEmail(String email);
}
