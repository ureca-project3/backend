package com.triple.backend.batch;

import com.triple.backend.batch.tasklet.syncFeedbackStep.MySqlFeedbackWriter;
import com.triple.backend.batch.tasklet.syncFeedbackStep.RedisFeedbackReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class SyncFeedbackStepTest {

    @Autowired
    private RedisFeedbackReader redisFeedbackReader;

    @Autowired
    private MySqlFeedbackWriter mySqlFeedbackWriter;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job syncFeedbackAndUpdateTraitsJob;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HashOperations<String, String, Set<Long>> hashOperations;

    private static final String LIKE_HASH_KEY = "likes";
    private static final String HATE_HASH_KEY = "hates";

    @BeforeEach
    void setUp() {
        hashOperations = redisTemplate.opsForHash();
        hashOperations.getOperations().delete(LIKE_HASH_KEY);
        hashOperations.getOperations().delete(HATE_HASH_KEY);
    }

    @DisplayName(value = "redis에서 mysql로 데이터 이관")
    @Test
    void success_sync_feedback_step() throws Exception {
        // given
        String memberId = "1";

        Set<Long> likeBooks = new HashSet<>();
        likeBooks.add(1L);
        likeBooks.add(2L);

        Set<Long> hateBooks = new HashSet<>();
        hateBooks.add(3L);

        hashOperations.put("likes", memberId, likeBooks);
        hashOperations.put("hates", memberId, hateBooks);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncher.run(syncFeedbackAndUpdateTraitsJob, jobParameters);

        // then
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // MySQL에 데이터가 잘 들어갔는지 검증한다 & 좋아요 싫어요가 제대로 처리되었는지도 함께 확인한다
        verifyFeedbackInDatabase(1L, 1L, true, false);
        verifyFeedbackInDatabase(1L, 2L, true, false);
        verifyFeedbackInDatabase(1L, 3L, false, true);

        // Redis에 데이터가 삭제되었는지 검증한다
        assertThat(hashOperations.entries("likes").isEmpty()).isTrue();
        assertThat(hashOperations.entries("hates").isEmpty()).isTrue();

    }

    @DisplayName(value = "redis에 데이터가 없어도 성공적 배치 실행")
    @Test
    void when_no_data_in_redis_step_completed() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncher.run(syncFeedbackAndUpdateTraitsJob, jobParameters);

        // then : redis에 data가 없으면 step이 그상태로 마무리된다
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");
    }


    @DisplayName(value = "bookId가 null로 삽입되었을 경우 배치 skip 기록")
    @Test
    void when_bookId_is_null_skip_exception() throws Exception {
        // given : bookId로 null과 같은 오류가 들어갔을 경우
        String memberId = "2";
        Set<Long> likeBooks = new HashSet<>();
        likeBooks.add(1L);
        likeBooks.add(2L);
        likeBooks.add(null);
        likeBooks.add(4L);
        likeBooks.add(5L);
        hashOperations.put("likes", memberId, likeBooks);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncher.run(syncFeedbackAndUpdateTraitsJob, jobParameters);

        // then : 무결성 오류 발생에도 skip 가능한 횟수 이내면 step이 수행되고 다른 데이터들이 잘 저장되는지 확인한다
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        verifyFeedbackInDatabase(2L, 1L, true, false);
        verifyFeedbackInDatabase(2L, 2L, true, false);
        verifyFeedbackInDatabase(2L, 4L, true, false);
        verifyFeedbackInDatabase(2L, 5L, true, false);

        // mysql에 bookI가 null로 들어간 경우가 없는 것을 확인한다
        verifyFeedbackNotInDatabase(2L, null);

    }

    @DisplayName(value = "에러로 인한 skip이 3번을 초과할 경우 job 실패")
    @Test
    void when_skip_over_three_times_step_failed() throws Exception {
        // given : bookId로 null과 같은 오류가 들어갔을 경우
        String memberId1 = "1";
        String memberId2 = "2";
        String memberId3 = "3";
        String memberId4 = "4";
        Set<Long> likeBooksForMember1 = new HashSet<>();

        likeBooksForMember1.add(1L);
        likeBooksForMember1.add(null);

        Set<Long> likeBooksForMember2 = new HashSet<>();
        likeBooksForMember2.add(null);

        Set<Long> likeBooksForMember3 = new HashSet<>();
        likeBooksForMember3.add(null);

        Set<Long> likeBooksForMember4 = new HashSet<>();
        likeBooksForMember4.add(null);

        hashOperations.put("likes", memberId1, likeBooksForMember1);
        hashOperations.put("likes", memberId2, likeBooksForMember2);
        hashOperations.put("likes", memberId3, likeBooksForMember3);
        hashOperations.put("likes", memberId4, likeBooksForMember4);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncher.run(syncFeedbackAndUpdateTraitsJob, jobParameters);

        // then : 무결성 오류 발생에도 skip 가능한 횟수 이내면 step이 수행되고 다른 데이터들이 잘 저장되는지 확인한다
        assertThat(jobExecution.getExitStatus().getExitCode()).isEqualTo("FAILED");

    }

    private void verifyFeedbackInDatabase(Long childId, Long bookId, boolean likeStatus, boolean hateStatus) {
        String sql = "SELECT count(*) FROM feedback WHERE child_id = ? AND book_id = ? AND like_status = ? AND hate_status = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, childId, bookId, likeStatus, hateStatus);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(1);
    }

    private void verifyFeedbackNotInDatabase(Long childId, Long bookId) {
        String sql = "SELECT count(*) FROM feedback WHERE child_id = ? AND book_id IS NULL";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, childId);
        assertThat(count).isEqualTo(0); // null인 경우 존재하지 않아야 함
    }

}
