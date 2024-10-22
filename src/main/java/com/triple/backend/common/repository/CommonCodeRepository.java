package com.triple.backend.common.repository;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    // 기존의 findById 메서드 (필요에 따라 유지)
    Optional<CommonCode> findById(CommonCodeId id);
}
