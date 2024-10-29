package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Mbti;
import com.triple.backend.test.entity.MbtiType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbtiRepository extends JpaRepository<Mbti, Long> {

    Optional<Mbti> findByName(MbtiType mbtiName);
}