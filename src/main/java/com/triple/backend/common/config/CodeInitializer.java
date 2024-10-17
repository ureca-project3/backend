package com.triple.backend.common.config;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.code.GroupCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class CodeInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) throws Exception {
        GroupCode groupCode1 = new GroupCode("100", "회원 관리");
        GroupCode groupCode2 = new GroupCode("200", "콘텐츠 장르");

        em.persist(groupCode1);
        em.persist(groupCode2);

        CommonCode commonCode1 = new CommonCode(new CommonCodeId("010", "100"), groupCode1, "회원", true);
        CommonCode commonCode2 = new CommonCode(new CommonCodeId("020", "100"), groupCode1, "관리자", true);

        em.persist(commonCode1);
        em.persist(commonCode2);

        em.flush();
    }
}
