package com.triple.backend.auth.service;

import com.triple.backend.auth.repository.CommonCodeRepository;
import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private CommonCodeRepository commonCodeRepository;

    public String getRoleByCodeId(String roleCodeId) throws RoleNotFoundException {
        CommonCodeId commonCodeId = new CommonCodeId(roleCodeId, "010");

        // Optional로 반환값 처리
        return commonCodeRepository.findById(commonCodeId)
                .map(CommonCode::getCommonName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found for codeId: " + roleCodeId));
    }
}