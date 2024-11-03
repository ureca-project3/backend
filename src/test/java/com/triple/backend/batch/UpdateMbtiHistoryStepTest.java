package com.triple.backend.batch;

import com.triple.backend.batch.dto.MbtiWithTraitScoreDto;
import com.triple.backend.common.utils.MbtiCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringBatchTest
//@Sql({"/clean-up.sql", "/batch.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UpdateMbtiHistoryStepTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job syncFeedbackAndUpdateTraitsJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(syncFeedbackAndUpdateTraitsJob);
    }

    @DisplayName(value = "자녀 성향이 바뀌었을 경우 MBTI 레코드 생성 성공")
    @Test
    void success_update_mbti_history_with_change() {
        // given
        MbtiWithTraitScoreDto dto1 = new MbtiWithTraitScoreDto(
                1L, 1L, 50, LocalDateTime.now(), "INTJ", 1L);
        MbtiWithTraitScoreDto dto2 = new MbtiWithTraitScoreDto(
                1L, 2L, 60, LocalDateTime.now(), "INTJ", 1L);
        MbtiWithTraitScoreDto dto3 = new MbtiWithTraitScoreDto(
                1L, 3L, 70, LocalDateTime.now(), "INTJ", 1L);
        MbtiWithTraitScoreDto dto4 = new MbtiWithTraitScoreDto(
                1L, 4L, 80, LocalDateTime.now(), "INTJ", 1L);
        List<MbtiWithTraitScoreDto> dtoList = new ArrayList<>(List.of(dto1, dto2, dto3, dto4));
        insertChildTraitsWithMbtiData(dtoList);

        String expectedMbti = MbtiCalculator.calculateNewMbti(dtoList);

        String stepName = "updateMbtiHistory";

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep(stepName);

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // MBTI가 변경된 경우에 대해 mbti_history에 기록이 남는지 검증
        assertMbtiHistoryUpdated(1L, expectedMbti);
    }

    @DisplayName(value = "자녀 성향이 바뀌지 않을 경우 레코드 생성 X")
    @Test
    void no_update_if_mbti_not_changed() {
        // given
        insertChildTraitsWithMbtiData(List.of(
                new MbtiWithTraitScoreDto(1L, 1L, 50, LocalDateTime.now(), "ENFP", 1L),
                new MbtiWithTraitScoreDto(1L, 2L, 60, LocalDateTime.now(), "ENFP", 1L),
                new MbtiWithTraitScoreDto(1L, 3L, 70, LocalDateTime.now(), "ENFP", 1L),
                new MbtiWithTraitScoreDto(1L, 4L, 80, LocalDateTime.now(), "ENFP", 1L)));

        String stepName = "updateMbtiHistory";

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep(stepName);

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // 기존 mbti와 같다면 update가 되지 않는다
        assertCurrentMbtiIsUnchanged(1L, "ENFP");
    }

    @DisplayName(value = "업데이트된 자녀 성향이 없을 경우")
    @Test
    void when_no_data_to_update() {
        String stepName = "updateMbtiHistory";

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep(stepName);

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }

    private void insertChildTraitsWithMbtiData(List<MbtiWithTraitScoreDto> dtoList) {
        String sql = """
            INSERT INTO child_traits (history_id, trait_id, trait_score, created_at)
            VALUES (?, ?, ?, NOW())
        """;

        for (MbtiWithTraitScoreDto dto : dtoList) {
            jdbcTemplate.update(sql, dto.getHistoryId(), dto.getTraitId(), dto.getTraitScore());
        }
    }


    private void assertMbtiHistoryUpdated(Long childId, String expectedMbti) {
        String sql = """
                SELECT current_mbti FROM mbti_history 
                WHERE child_id = ? 
                ORDER BY created_at DESC LIMIT 1
            """;
        String actualMbti = jdbcTemplate.queryForObject(sql, String.class, childId);

        assertThat(actualMbti).isEqualTo(expectedMbti);
    }

    private void assertCurrentMbtiIsUnchanged(Long childId, String expectedMbti) {
        String sql = """
            SELECT current_mbti FROM mbti_history 
            WHERE child_id = ?
            ORDER BY created_at DESC
            LIMIT 1
        """;
        String currentMbti = jdbcTemplate.queryForObject(sql, String.class, childId);

        assertThat(currentMbti).isEqualTo(expectedMbti);
    }
}
