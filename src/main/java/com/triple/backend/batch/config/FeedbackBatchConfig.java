package com.triple.backend.batch.config;

import com.triple.backend.batch.dto.FeedbackDto;
import com.triple.backend.batch.dto.TraitsChangeDto;
import com.triple.backend.batch.exception.CustomSkipListener;
import com.triple.backend.batch.tasklet.FeedbackProcessor;
import com.triple.backend.batch.tasklet.FeedbackReader;
import com.triple.backend.batch.tasklet.FeedbackWriter;
import com.triple.backend.batch.tasklet.TraitsChangeItemWriter;
import com.triple.backend.feedback.entity.Feedback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class FeedbackBatchConfig {

    private final FeedbackReader feedbackReader;
    private final FeedbackProcessor feedbackProcessor;
    private final TraitsChangeItemWriter traitsChangeItemWriter;
    private final FeedbackWriter feedbackWriter;

    /*
    배치 작업을 설정하는 부분

    배치 작업의 이름은 syncFeedbackAndUpdateTraitsJob. 이 작업은 jobRepsitory와 syncFeedbackStep으로 구성된다.

    .start() 배치 작업의 첫 번째 스텝으로 syncFeedbackStep을 지정한다. 이 스텝에서 실제 데이터 처리 과정이 이루어진다.
    .incrementer(new RunIdIncrementer()) 각 배치 작업 시 고유 ID를 추가한다.
    개발 중 같은 배치 작업을 테스트해 가며 사용할 때 쓰며, 매 실행을 구분해야 할 때도 사용한다.
     */
    @Bean
    public Job syncFeedbackAndUpdateTraitsJob(JobRepository jobRepository,
                                              Step processFeedbackStep, Step syncFeedbackToMySQLStep) {
        /* 문제1 : feedbackReader를 각 스텝마다 실행해야 한다.
        해결책에 대한 고민 : Step 끼리 연결이 되어 있으면, 서로 데이터를 전달할 수도 있지 않을까? 해당 키워드 찾아보기
        JobExecutionContext: Job의 여러 Step에서 공유되는 데이터를 저장하는 컨텍스트입니다. 이를 사용하면 여러 Step 간에 데이터를 주고받을 수 있습니다.
        StepExecutionContext: Step 내에서만 유효한 데이터를 저장합니다. 같은 Step에서 실행 중인 여러 청크 사이에서 데이터를 공유할 때 사용할 수 있습니다.
        */
        return new JobBuilder("syncFeedbackAndUpdateTraitsJob", jobRepository)
                .start(processFeedbackStep)
                .next(syncFeedbackToMySQLStep)
                .incrementer(new RunIdIncrementer()) // 실제 사용할 때는 삭제 필요
                .build();
    }

    /* 문제2 : main datasource 와 batch datasource를 구분해서 적용해야 한다.
    해결책에 대한 고민 : 각 reader, processor, writer에 파라미터로 적절한 datasource를 넣어 주어야 한다.
    현재 feedbackProcessor, traitsChangeItemWriter에 mainDataSource가 들어가야 한다.
    */
    @Bean
    public Step processFeedbackStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    @Qualifier("mainDataSource") DataSource dataSource) {
        return new StepBuilder("processFeedbackStep", jobRepository)
                .<List<FeedbackDto>, List<TraitsChangeDto>>chunk(100, transactionManager)
                .reader(feedbackReader)
                .processor(feedbackProcessor)
                .writer(traitsChangeItemWriter)
                .faultTolerant() // 오류가 발생하더라도 실행을 중단하지 않고 오류 처리
                .skip(Exception.class) // 하위 예외 발생할 경우 해당 항목을 스킵하고 계속 실행
                .skipLimit(10) // 하지만 스킵 개수는 최대 10개까지. 그 이상이면 job 실패
                .listener(new CustomSkipListener("processFeedbackStep")) // 예외 발생시 리스너가 지정된 로직 수행하도록
                .build();
    }

    @Bean
    public Step syncFeedbackToMySQLStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    @Qualifier("mainDataSource") DataSource dataSource) {
        return new StepBuilder("syncFeedbackToMySQLStep", jobRepository)
                .<List<FeedbackDto>, Feedback>chunk(100, transactionManager)
                .reader(feedbackReader)
                .writer(feedbackWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .listener(new CustomSkipListener("syncFeedbackToMySQLStep"))
                .build();
    }
}
