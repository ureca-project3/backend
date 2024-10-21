package com.triple.backend.auth.repository;

import com.triple.backend.auth.entity.RefreshToken;
import com.triple.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // memberId를 사용하여 RefreshToken을 찾는 메서드
    @Query("SELECT r FROM RefreshToken r WHERE r.member.memberId = :memberId")
    Optional<RefreshToken> findByMemberId(Long memberId);

    // memberId를 사용하여 RefreshToken 삭제
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.member.memberId = :memberId")
    void deleteByMemberId(Long memberId);
}