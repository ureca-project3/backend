package com.triple.backend.batch.config;

import com.triple.backend.batch.dto.*;
import com.triple.backend.batch.mapper.FeedbackAndTraitsRowMapper;
import com.triple.backend.batch.mapper.TraitsChangeRowMapper;
import com.triple.backend.batch.tasklet.syncFeedbackStep.MySqlFeedbackWriter;
import com.triple.backend.batch.tasklet.syncFeedbackStep.RedisFeedbackReader;
import com.triple.backend.batch.tasklet.updateMbtiHistory.MbtiProcessor;
import com.triple.backend.batch.tasklet.updateMbtiHistory.MbtiReader;
import com.triple.backend.common.utils.MbtiCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchConfig extends DefaultBatchConfiguration {

    private final RedisFeedbackReader feedbackReader;
    private final MySqlFeedbackWriter feedbackWriter;

    private final MbtiReader mbtiReader;
    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Bean
    public Job syncFeedbackAndUpdateTraitsJob(JobRepository jobRepository,
                                              Step syncFeedbackStep,
                                              Step updateTraitsChange,
                                              Step updateChildTraits,
                                              Step updateMbtiHistory
    ) {
        return new JobBuilder("syncFeedbackAndUpdateTraitsJob", jobRepository)
                .start(syncFeedbackStep)
                .next(updateTraitsChange)
                .next(updateChildTraits)
                .next(updateMbtiHistory)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step syncFeedbackStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("syncFeedbackStep", jobRepository)
                .<FeedbackDto, FeedbackDto>chunk(1, transactionManager)
                .reader(feedbackReader)
                .writer(feedbackWriter)
                .build();
    }

    @Bean
    public Step updateTraitsChange(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updateTraitsChange", jobRepository)
                .<FeedbackAndTraitsDto, List<TraitsChangeDto>>chunk(1, transactionManager)
                .reader(mySQLFeedbackReader())
                .processor(traitsChangeProcessor())
                .writer(traitsChangeWriter())
                .build();
    }

    @Bean
    public Step updateChildTraits(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updateChildTraits", jobRepository)
                .<TraitsChangeDto, TraitsChangeDto>chunk(1, transactionManager)
                .reader(traitsChangeReader())
                .writer(childTraitsWriter())
                .build();
    }

    @Bean
    public Step updateMbtiHistory(JobRepository jobRepository, PlatformTransactionManager transactionManager, MbtiProcessor mbtiProcessor) {
        return new StepBuilder("updateMbtiHistory", jobRepository)
                .<List<MbtiWithTraitScoreDto>, MbtiDto>chunk(1, transactionManager)
                .reader(mbtiReader)
                .processor(mbtiProcessor)
                .writer(mbtiWriter())
                .build();
    }

    @Bean
    public JdbcCursorItemReader<FeedbackAndTraitsDto> mySQLFeedbackReader() {
        JdbcCursorItemReader<FeedbackAndTraitsDto> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("""
        SELECT f.child_id, f.book_id, f.like_status, f.hate_status
        FROM feedback f
        WHERE DATE(f.created_at) = CURDATE()
        """);
        reader.setRowMapper(new FeedbackAndTraitsRowMapper(namedParameterJdbcTemplate));
        return reader;
    }

    @Bean
    public ItemProcessor<FeedbackAndTraitsDto, List<TraitsChangeDto>> traitsChangeProcessor() {
        return new ItemProcessor<FeedbackAndTraitsDto, List<TraitsChangeDto>>() {
            @Override
            public List<TraitsChangeDto> process(FeedbackAndTraitsDto dto) throws Exception {

                Long childId = dto.getChildId();
                List<ChildTraitsDto> childTraits = dto.getChildTraits();
                List<BookTraitsDto> bookTraits = dto.getBookTraits();

                List<TraitsChangeDto> traitsChangeDtos = new ArrayList<>();

                for (int i = 0; i < childTraits.size(); i++) {
                    ChildTraitsDto childTrait = childTraits.get(i);
                    BookTraitsDto bookTrait = bookTraits.get(i);

                    Integer changeAmount = MbtiCalculator.calculateTraitChange(
                            childTrait.getTraitScore(),
                            bookTrait.getBookTraitScore()
                    );

                    // 계산된 changeAmount 확인
                    System.out.println("Processor에서 계산된 changeAmount: " + changeAmount
                            + " (childId: " + childId + ", traitId: " + childTrait.getTraitId() + ")");


                    TraitsChangeDto traitsChangeDto = new TraitsChangeDto();
                    traitsChangeDto.setChildId(childId);
                    traitsChangeDto.setTraitId(childTrait.getTraitId());
                    traitsChangeDto.setChangeAmount(changeAmount);

                    traitsChangeDtos.add(traitsChangeDto);
                }

                return traitsChangeDtos;
            }
        };
    }

    @Bean
    public ItemWriter<List<TraitsChangeDto>> traitsChangeWriter() {
        return new ItemWriter<List<TraitsChangeDto>>() {
            @Override
            public void write(Chunk<? extends List<TraitsChangeDto>> chunk) throws Exception {
                String updateSql = """
                UPDATE traits_change
                SET change_amount = change_amount + :changeAmount
                WHERE child_id = :childId AND trait_id = :traitId
                """;

                List<Map<String, Object>> batchValues = new ArrayList<>();
                for (List<TraitsChangeDto> traitsChangeDtos : chunk) {
                    for (TraitsChangeDto traitsChangeDto : traitsChangeDtos) {
//                        System.out.println("Writer에서의 childId: " + traitsChangeDto.getChildId());
//                        System.out.println("Writer에서의 traitId: " + traitsChangeDto.getTraitId());
//                        System.out.println("Writer에서의 changeAmount: " + traitsChangeDto.getChangeAmount());
                        Map<String, Object> params = Map.of(
                                "childId", traitsChangeDto.getChildId(),
                                "traitId", traitsChangeDto.getTraitId(),
                                "changeAmount", traitsChangeDto.getChangeAmount()
                        );
                        batchValues.add(params);
                    }
                }

                namedParameterJdbcTemplate.batchUpdate(updateSql, batchValues.toArray(new Map[0]));
            }
        };
    }

    @Bean
    public JdbcCursorItemReader<TraitsChangeDto> traitsChangeReader() {
        JdbcCursorItemReader<TraitsChangeDto> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("""
        SELECT tc.trait_change_id, tc.child_id, tc.trait_id, tc.change_amount
        FROM traits_change tc
        WHERE change_amount >= 5
        """);
        reader.setRowMapper(new TraitsChangeRowMapper());
        return reader;
    }

    @Bean
    public ItemWriter<TraitsChangeDto> childTraitsWriter() {
        return new ItemWriter<>() {
            @Override
            public void write(Chunk<? extends TraitsChangeDto> chunk) throws Exception {
                // 1. ChildTraits에 새로운 레코드 생성
                String insertChildTraitsSql = """
                INSERT INTO child_traits (history_id, trait_id, trait_score, created_at)
                SELECT
                    (SELECT history_id
                     FROM mbti_history
                     WHERE child_id = :childId
                     ORDER BY created_at DESC
                     LIMIT 1) AS history_id,
                    :traitId AS trait_id,
                    (SELECT trait_score
                     FROM (SELECT trait_score 
                           FROM child_traits
                           WHERE history_id = (SELECT history_id
                                               FROM mbti_history
                                               WHERE child_id = :childId
                                               ORDER BY created_at DESC
                                               LIMIT 1)
                             AND trait_id = :traitId
                           ORDER BY created_at DESC
                           LIMIT 1) AS score_subquery
                    ) + :changeAmount AS trait_score,
                    :createdAt AS created_at
            """;

                List<Map<String, Object>> insertChildTraitsParams = new ArrayList<>();
                for (TraitsChangeDto dto : chunk) {
                    Map<String, Object> params = Map.of(
                            "childId", dto.getChildId(),
                            "traitId", dto.getTraitId(),
                            "changeAmount", dto.getChangeAmount(),
                            "createdAt", LocalDateTime.now()
                    );
                    insertChildTraitsParams.add(params);
                }

                namedParameterJdbcTemplate.batchUpdate(insertChildTraitsSql, insertChildTraitsParams.toArray(new Map[0]));

                // 2. TraitsChange 초기화
                String resetTraitsChangeSql = """
                UPDATE traits_change
                SET change_amount = 0
                WHERE trait_change_id = :traitChangeId
                """;

                List<Map<String, Object>> resetTraitsChangeParams = new ArrayList<>();
                for (TraitsChangeDto dto : chunk) {
                    Map<String, Object> params = Map.of("traitChangeId", dto.getTraitChangeId());
                    resetTraitsChangeParams.add(params);
                }

                namedParameterJdbcTemplate.batchUpdate(resetTraitsChangeSql, resetTraitsChangeParams.toArray(new Map[0]));
            }
        };
    }

    @Bean
    public ItemWriter<MbtiDto> mbtiWriter() {
        return (Chunk<? extends MbtiDto> items) -> {
            if (items == null || items.isEmpty()) {
                return; // 데이터가 없으면 반환
            }

            String sql = """
            INSERT INTO mbti_history (child_id, current_mbti, reason, reason_id, is_deleted, created_at, modified_at)
            VALUES (:childId, :currentMbti, :changeReason, :changeReasonId, :isDeleted, :createdAt, :modifiedAt)
        """;

            // Chunk의 각 요소를 List로 변환
            List<Map<String, Object>> batchValues = new ArrayList<>();
            for (MbtiDto mbtiDto : items.getItems()) {  // getItems()를 통해 실제 요소를 순회
                Map<String, Object> paramMap = Map.of(
                        "childId", mbtiDto.getChildId(),
                        "currentMbti", mbtiDto.getCurrentMbti(),
                        "changeReason", mbtiDto.getChangeReason(),
                        "changeReasonId", mbtiDto.getChangeReasonId(),
                        "isDeleted", mbtiDto.getIsDeleted(),
                        "createdAt", LocalDateTime.now(),
                        "modifiedAt", LocalDateTime.now()
                );
                batchValues.add(paramMap);
            }

            namedParameterJdbcTemplate.batchUpdate(sql, batchValues.toArray(new Map[0]));
        };
    }

}
