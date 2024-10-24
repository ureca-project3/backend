package com.triple.backend.test.repository;

import com.triple.backend.test.entity.Mbti;
import com.triple.backend.test.entity.MbtiType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MbtiRepository extends JpaRepository<Mbti, Long> {

    Mbti findByName(MbtiType mbtiName);
}