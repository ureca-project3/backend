package com.triple.backend.common.config;

import com.triple.backend.common.code.CommonCode;
import com.triple.backend.common.code.CommonCodeId;
import com.triple.backend.common.code.GroupCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class CodeInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void run(String... args) {
        List<GroupCode> groupCodes = em.createQuery("SELECT gc FROM GroupCode gc", GroupCode.class)
                .getResultList();

        if (!groupCodes.isEmpty()) {
            return;
        }

        GroupCode groupCode1 = new GroupCode("100", "회원 관리");
        GroupCode groupCode2 = new GroupCode("200", "콘텐츠 장르");
        GroupCode groupCode3 = new GroupCode("300", "히스토리 변경 원인");

        em.persist(groupCode1);
        em.persist(groupCode2);
        em.persist(groupCode3);

        CommonCode commonCode1 = new CommonCode(new CommonCodeId("010", "100"), groupCode1, "회원", true);
        CommonCode commonCode2 = new CommonCode(new CommonCodeId("020", "100"), groupCode1, "관리자", true);

        CommonCode commonCode3 = new CommonCode(new CommonCodeId("010", "200"), groupCode2, "동화", true);
        CommonCode commonCode4 = new CommonCode(new CommonCodeId("011", "200"), groupCode2, "브랜드 전집", true);
        CommonCode commonCode5 = new CommonCode(new CommonCodeId("012", "200"), groupCode2, "명작동화", true);
        CommonCode commonCode6 = new CommonCode(new CommonCodeId("013", "200"), groupCode2, "전래동화", true);
        CommonCode commonCode7 = new CommonCode(new CommonCodeId("014", "200"), groupCode2, "창작동화", true);
        CommonCode commonCode8 = new CommonCode(new CommonCodeId("015", "200"), groupCode2, "위인전", true);
        CommonCode commonCode9 = new CommonCode(new CommonCodeId("016", "200"), groupCode2, "공주/왕자", true);
        CommonCode commonCode10 = new CommonCode(new CommonCodeId("017", "200"), groupCode2, "모험/탐험", true);
        CommonCode commonCode11 = new CommonCode(new CommonCodeId("018", "200"), groupCode2, "유명작가", true);
        CommonCode commonCode12 = new CommonCode(new CommonCodeId("020", "200"), groupCode2, "자연", true);
        CommonCode commonCode13 = new CommonCode(new CommonCodeId("021", "200"), groupCode2, "계절/날씨", true);
        CommonCode commonCode14 = new CommonCode(new CommonCodeId("022", "200"), groupCode2, "산/바다", true);
        CommonCode commonCode15 = new CommonCode(new CommonCodeId("023", "200"), groupCode2, "하늘/우주", true);
        CommonCode commonCode16 = new CommonCode(new CommonCodeId("024", "200"), groupCode2, "식물", true);
        CommonCode commonCode17 = new CommonCode(new CommonCodeId("030", "200"), groupCode2, "배우기", true);
        CommonCode commonCode18 = new CommonCode(new CommonCodeId("031", "200"), groupCode2, "한글", true);
        CommonCode commonCode19 = new CommonCode(new CommonCodeId("032", "200"), groupCode2, "수학", true);
        CommonCode commonCode20 = new CommonCode(new CommonCodeId("033", "200"), groupCode2, "과학", true);
        CommonCode commonCode21 = new CommonCode(new CommonCodeId("034", "200"), groupCode2, "역사/문화", true);
        CommonCode commonCode22 = new CommonCode(new CommonCodeId("035", "200"), groupCode2, "무료워크지", true);
        CommonCode commonCode23 = new CommonCode(new CommonCodeId("036", "200"), groupCode2, "퀴즈", true);
        CommonCode commonCode24 = new CommonCode(new CommonCodeId("037", "200"), groupCode2, "생활/안전", true);
        CommonCode commonCode25 = new CommonCode(new CommonCodeId("038", "200"), groupCode2, "성교육", true);
        CommonCode commonCode26 = new CommonCode(new CommonCodeId("040", "200"), groupCode2, "동물", true);
        CommonCode commonCode27 = new CommonCode(new CommonCodeId("041", "200"), groupCode2, "동물", true);
        CommonCode commonCode28 = new CommonCode(new CommonCodeId("042", "200"), groupCode2, "곤충", true);
        CommonCode commonCode29 = new CommonCode(new CommonCodeId("043", "200"), groupCode2, "파충류", true);
        CommonCode commonCode30 = new CommonCode(new CommonCodeId("044", "200"), groupCode2, "공룡", true);
        CommonCode commonCode31 = new CommonCode(new CommonCodeId("045", "200"), groupCode2, "새", true);
        CommonCode commonCode32 = new CommonCode(new CommonCodeId("046", "200"), groupCode2, "반려동물", true);
        CommonCode commonCode33 = new CommonCode(new CommonCodeId("047", "200"), groupCode2, "바다동물", true);
        CommonCode commonCode34 = new CommonCode(new CommonCodeId("048", "200"), groupCode2, "물고기", true);
        CommonCode commonCode35 = new CommonCode(new CommonCodeId("050", "200"), groupCode2, "일상생활", true);
        CommonCode commonCode36 = new CommonCode(new CommonCodeId("051", "200"), groupCode2, "가족", true);
        CommonCode commonCode37 = new CommonCode(new CommonCodeId("052", "200"), groupCode2, "친구", true);
        CommonCode commonCode38 = new CommonCode(new CommonCodeId("053", "200"), groupCode2, "똥/방귀", true);
        CommonCode commonCode39 = new CommonCode(new CommonCodeId("054", "200"), groupCode2, "잠자기", true);
        CommonCode commonCode40 = new CommonCode(new CommonCodeId("055", "200"), groupCode2, "탈것", true);
        CommonCode commonCode41 = new CommonCode(new CommonCodeId("056", "200"), groupCode2, "음식", true);
        CommonCode commonCode42 = new CommonCode(new CommonCodeId("057", "200"), groupCode2, "놀이/체험", true);
        CommonCode commonCode43 = new CommonCode(new CommonCodeId("058", "200"), groupCode2, "그림/음악", true);

        CommonCode commonCode44 = new CommonCode(new CommonCodeId("010", "300"), groupCode3, "자녀 성향 진단", true);
        CommonCode commonCode45 = new CommonCode(new CommonCodeId("020", "300"), groupCode3, "피드백", true);

        em.persist(commonCode1);
        em.persist(commonCode2);

        em.persist(commonCode3);
        em.persist(commonCode4);
        em.persist(commonCode5);
        em.persist(commonCode6);
        em.persist(commonCode7);
        em.persist(commonCode8);
        em.persist(commonCode9);
        em.persist(commonCode10);
        em.persist(commonCode11);
        em.persist(commonCode12);
        em.persist(commonCode13);
        em.persist(commonCode14);
        em.persist(commonCode15);
        em.persist(commonCode16);
        em.persist(commonCode17);
        em.persist(commonCode18);
        em.persist(commonCode19);
        em.persist(commonCode20);
        em.persist(commonCode21);
        em.persist(commonCode22);
        em.persist(commonCode23);
        em.persist(commonCode24);
        em.persist(commonCode25);
        em.persist(commonCode26);
        em.persist(commonCode27);
        em.persist(commonCode28);
        em.persist(commonCode29);
        em.persist(commonCode30);
        em.persist(commonCode31);
        em.persist(commonCode32);
        em.persist(commonCode33);
        em.persist(commonCode34);
        em.persist(commonCode35);
        em.persist(commonCode36);
        em.persist(commonCode37);
        em.persist(commonCode38);
        em.persist(commonCode39);
        em.persist(commonCode40);
        em.persist(commonCode41);
        em.persist(commonCode42);
        em.persist(commonCode43);

        em.persist(commonCode44);
        em.persist(commonCode45);

        em.flush();
    }
}
