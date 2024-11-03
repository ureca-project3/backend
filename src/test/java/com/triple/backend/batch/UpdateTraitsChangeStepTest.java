package com.triple.backend.batch;

import com.triple.backend.common.utils.MbtiCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@SpringBatchTest
@Sql({"/clean-up.sql", "/batch.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UpdateTraitsChangeStepTest {

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

    @DisplayName(value = "성향 변화량 누적치 업데이트 성공")
    @Test
    void success_update_traits_change() throws Exception {
        // given
        insertSqlForTestUpdateTraits();

        String stepName = "updateTraitsChange";

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep(stepName);

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // TraitsChange 테이블에 데이터 반영값을 검증한다
        verifyTraitsChangeInDatabase(1L, 1L, calculateExpectedChangeAmount(50, 70));
        verifyTraitsChangeInDatabase(1L, 2L, calculateExpectedChangeAmount(50, 35));
        verifyTraitsChangeInDatabase(1L, 3L, calculateExpectedChangeAmount(50, 20));
        verifyTraitsChangeInDatabase(1L, 4L, calculateExpectedChangeAmount(50, 60));
    }

    @DisplayName(value = "성향이 100이거나 0일 경우 데이터 처리 확인")
    @Test
    void success_when_number_on_boundary() throws Exception {
        // given
        String insertFeedbackSql = """
                INSERT INTO feedback (child_id, book_id, like_status, hate_status, created_at)
                VALUES (1, 1, true, false, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.execute(insertFeedbackSql);
        insertChildTraitsForEdgeCase(0, 100);
        insertBookTraitsForEdgeCase(0, 100);

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("updateTraitsChange");

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // 0이나 100같은 경계값에서도 배치가 잘 동작하는지 검증한다
        verifyTraitsChangeInDatabase(1L, 1L, calculateExpectedChangeAmount(0, 0));
        verifyTraitsChangeInDatabase(1L, 2L, calculateExpectedChangeAmount(100, 0));
        verifyTraitsChangeInDatabase(1L, 3L, calculateExpectedChangeAmount(0, 100));
        verifyTraitsChangeInDatabase(1L, 4L, calculateExpectedChangeAmount(100, 100));
    }

    @DisplayName(value = "성향 개수가 4개가 아닌 경우 skip 발생")
    @Test
    void when_traits_size_is_wrong() {
        // given
        String insertFeedbackSql = """
                INSERT INTO feedback (child_id, book_id, like_status, hate_status, created_at)
                VALUES (1, 1, true, false, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.execute(insertFeedbackSql);
        insertChildTraits(50, 50, 50, 50);
        insertNotEnoughBookTraits(60, 70, 80);

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("updateTraitsChange");

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // skiplistener가 실행되어 job은 성공적으로 마치지만, skip count가 1 증가하는지 확인한다
        Long skipCount = stepExecution.getStepExecutions().stream()
                .filter(step -> step.getStepName().equals("updateTraitsChange"))
                .findFirst()
                .map(StepExecution::getSkipCount)
                .orElse(0L);

        assertThat(skipCount).isEqualTo(1L);
    }

    @DisplayName(value = "좋아요/싫어요 데이터가 존재하지 않는 경우")
    @Test
    void when_no_data_returns_from_reader() {
        // given
        Long childId = 1L;
        Long traitId1 = 1L;

        // when
        JobExecution stepExecution = jobLauncherTestUtils.launchStep("updateTraitsChange");

        // then
        assertThat(stepExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Reader로부터 넘어온 FeedbackWithTraitsDto가 없을 경우, TraitsChange 테이블에 변화가 없는지 확인한다
        String sql = "SELECT change_amount FROM traits_change WHERE child_id = ? AND trait_id = ?";
        Integer changeAmount = jdbcTemplate.queryForObject(sql, Integer.class, childId, traitId1);
        assertThat(changeAmount).isEqualTo(0);
    }

    private void insertSqlForTestUpdateTraits() {
        insertFeedbackSql();

        String insertChildTraitsSql = """
                INSERT INTO child_traits (history_id, trait_id, trait_score, created_at)
                VALUES (1, 1, 50, CURRENT_TIMESTAMP),
                       (1, 2, 50, CURRENT_TIMESTAMP),
                       (1, 3, 50, CURRENT_TIMESTAMP),
                       (1, 4, 50, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.execute(insertChildTraitsSql);

        String insertBookTraitsSql = """
                INSERT INTO book_traits (book_id, trait_id, trait_score)
                VALUES (1, 1, 70),
                       (1, 2, 35),
                       (1, 3, 20),
                       (1, 4, 60)
            """;
        jdbcTemplate.execute(insertBookTraitsSql);
    }

    private void insertFeedbackSql() {
        String insertFeedbackSql = """
                INSERT INTO feedback (child_id, book_id, like_status, hate_status, created_at)
                VALUES (1, 1, true, false, CURRENT_TIMESTAMP),
                       (1, 2, false, true, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.execute(insertFeedbackSql);
    }

    private void insertChildTraitsForEdgeCase(int zero, int hundred) {
        String insertChildTraitsSql = """
                INSERT INTO child_traits (history_id, trait_id, trait_score, created_at)
                VALUES (1, 1, ?, CURRENT_TIMESTAMP),
                       (1, 2, ?, CURRENT_TIMESTAMP),
                       (1, 3, ?, CURRENT_TIMESTAMP),
                       (1, 4, ?, CURRENT_TIMESTAMP)
            """;
        jdbcTemplate.update(insertChildTraitsSql, zero, hundred, zero, hundred);
    }

    private void insertBookTraitsForEdgeCase(int zero, int hundred) {
        String insertBookTraitsSql = """
                INSERT INTO book_traits (book_id, trait_id, trait_score)
                VALUES (1, 1, ?),
                       (1, 2, ?),
                       (1, 3, ?),
                       (1, 4, ?)
            """;
        jdbcTemplate.update(insertBookTraitsSql, zero, zero, hundred, hundred);
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

    private void insertWrongBookTraits() {
        String insertBookTraitsSql = """
                INSERT INTO book_traits (book_id, trait_id, trait_score)
                VALUES (1, 1, null),
                       (1, 2, null),
                       (1, 3, null),
                       (1, 4, null)
            """;
        jdbcTemplate.update(insertBookTraitsSql);
    }

    private void insertNotEnoughBookTraits(int trait1, int trait2, int trait3) {
        String insertBookTraitsSql = """
                INSERT INTO book_traits (book_id, trait_id, trait_score)
                VALUES (1, 1, ?),
                       (1, 2, ?),
                       (1, 3, ?)
            """;
        jdbcTemplate.update(insertBookTraitsSql, trait1, trait2, trait3);
    }

    private void verifyTraitsChangeInDatabase(Long childId, Long traitId, int expectedChangeAmount) {
        String sql = "SELECT change_amount FROM traits_change WHERE child_id = ? AND trait_id = ?";
        Integer changeAmount = jdbcTemplate.queryForObject(sql, Integer.class, childId, traitId);
        assertThat(changeAmount).isEqualTo(expectedChangeAmount);
    }

    private int calculateExpectedChangeAmount(int childTrait, int bookTrait) {
        return  MbtiCalculator.calculateTraitChange(childTrait, bookTrait);
    }
}
