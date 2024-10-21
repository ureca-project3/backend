package com.triple.backend.auth.repository;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {


    // 특정 역할 코드 ID에 해당하는 공통코드 조회
    Optional <CommonCode> findById(CommonCodeId id);
}