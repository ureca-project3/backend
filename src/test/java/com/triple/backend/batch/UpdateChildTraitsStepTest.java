package com.triple.backend.batch;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringBatchTest
@Sql({"/clean-up.sql", "/batch.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UpdateChildTraitsStepTest {

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

    @DisplayName(value = "성향 변화 누적치가 5이상일 경우 자녀 성향 레코드 생성 성공")
    @Test
    void success_update_child_traits() {
        // given
        insertTraitsChange(1, 3, 5, 7);
        insertChildTraits(50, 50, 50, 50);

        String stepName = "updateChildTraits";

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep(stepName);

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // childTraits 테이블에 데이터 반영값을 검증한다
        assertChildTraitsUpdated(1L, List.of(50, 50, 55, 57));

    }

    @DisplayName(value = "자녀 성향이 음수일 때 0으로 변환 확인")
    @Test
    void update_child_traits_with_negative_values() {
        // given
        insertTraitsChange(-3, -6, 5, 8);
        insertChildTraits(0, 0, 0, 0);

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("updateChildTraits");

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // childTraits가 음수로 내려가면 0을 반환해야 한다
        assertChildTraitsUpdated(1L, List.of(0, 0, 5, 8));
    }

    @DisplayName(value = "자녀 성향이 100을 초과할 때 100으로 변환 확인")
    @Test
    void update_child_traits_with_maximum_boundary() {
        // given
        insertTraitsChange(1, 1, 1, 5);
        insertChildTraits(50, 50, 50, 99);

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("updateChildTraits");

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // childTraits가 100을 넘어가면 100을 반환해야 한다
        assertChildTraitsUpdated(1L, List.of(50, 50, 50, 100));
    }

    private void assertChildTraitsUpdated(Long historyId, List<Integer> expectedScores) {
        String sql = """
            SELECT trait_score 
            FROM child_traits ct 
            WHERE ct.history_id = ? 
            AND (ct.created_at, ct.trait_id) 
                IN (SELECT MAX(created_at), trait_id 
                    FROM child_traits 
                    WHERE history_id = ? 
                    GROUP BY trait_id) 
            ORDER BY trait_id
            """;

        List<Integer> actualScores = jdbcTemplate.queryForList(sql, Integer.class, historyId, historyId);

        assertThat(actualScores).isEqualTo(expectedScores);
    }

    private void insertTraitsChange(int traitsChange1, int traitsChange2, int traitsChange3, int traitsChange4) {
        String insertTraitsChangeSql = """
                INSERT INTO traits_change (change_amount, child_id, created_at, modified_at, trait_id)
                VALUES
                    (?, 1, NOW(), NOW(), 1),
                    (?, 1, NOW(), NOW(), 2),
                    (?, 1, NOW(), NOW(), 3),
                    (?, 1, NOW(), NOW(), 4);
                """;

        jdbcTemplate.update(insertTraitsChangeSql, traitsChange1, traitsChange2, traitsChange3, traitsChange4);
    }

    private void insertChildTraits(int trait1, int trait2, int trait3, int trait4) {
        String insertChildTraitsSql = """
                INSERT INTO child_traits (history_id, trait_id, trait_score, created_at)
                VALUES (1, 1, ?, CURRENT_TIMESTAMP),
                       (1, 2, ?, CURRENT_TIMESTAMP),
                       (1, 3, ?, CURRENT_TIMESTAMP),
                       (1, 4, ?, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.update(insertChildTraitsSql, trait1, trait2, trait3, trait4);
    }
}
